package com.example.wenshi;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class WifiActivity extends AppCompatActivity {
    final private int UDP_RECEIVE_BUFFER_LENGTH = 1024;
    final private int SENSOR_FRAME_LENGTH = 15;
    final private String TDS_LINE_COLOR = "#0000FF";
    final private String TEMP_LINE_COLOR = "#00FF00";
    final private String HUM_LINE_COLOR = "#FF0000";
    final private String LIGHT_LINE_COLOR = "#00FFFF";
    private SQLiteDatabase dbWriter;
    private MyDBOpenHelper dbOpenHelper;
    private NotificationManager manager;
    private int SIMPLE_NOTFICATION_ID=1600; //notification的ID，整数
    private int NEW_NOTFICATION_ID=1800; //notification的ID，整数
    NotificationCompat.Builder builder;
    private int[] frameBuffer = new int[SENSOR_FRAME_LENGTH];
    private int frameByteCount = 0;
    private Socket socket;
    private LineChart turangwenduChart;
    private LineChart turangshiduChart;
    private LineChart huanjingshiduChart;
    private LineChart guangqiangChart;
    private LineChart huanjingwenduChart;
    private LineChart dianliuChart;
    private Handler mMainHandler;
    private TextView turangwendu;
    private TextView turangshidu;
    private TextView huanjingshidu;
    private TextView guangqiang;
    private TextView huanjingwendu;
    private TextView dianliu;
    String tuwen,tushi,huanwen,huanshi,guangq,dianl,shijian,jiedianwei;
    String waizheyang_state="0",dingkaichuang_state="0",cekaichuang_state="0",shilian_state="0",zhouliufengji_state="0";
    String neizheyang_state="0";
    String huanliufengji_state="0",wuhua_state,guangai_state="0";
    String shebei="1";
    int year,month,day,hour,minute,second;
    int tuwen1,tushi1,huanwen1,huanshi1,guangq1,dianl1,jiedianwei_int;
    String response="#00000000000000000000000";
    private String pcIp;
    private String pcPort;
    private int pcport;
    private Handler handler;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    private DatagramSocket udpSocket;

    private ServerSocket tcpServerSocket;
    private Socket mcuTcpSocket;
    private InputStream mcuTcpInputStream;

    private Socket pcTcpSocket;
    private OutputStream pcTcpOutputStream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
        builder=new NotificationCompat.Builder(this,"default");
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        initChart(turangwenduChart);
        initChart(turangshiduChart);
        initChart(huanjingshiduChart);
        initChart(guangqiangChart);
        initChart(huanjingwenduChart);
        initChart(dianliuChart);
        //初始化数据库
        dbOpenHelper = new MyDBOpenHelper(getApplicationContext(), "SC_Database.db", null, 1);
        dbWriter = dbOpenHelper.getWritableDatabase();
        Intent intent = getIntent();
        Bundle bundle =intent.getExtras();
        pcIp = bundle.getString("PC_IP");
        pcPort=bundle.getString("PC_PORT");
        pcport = Integer.parseInt(pcPort);
        tcpStart();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        turangwendu.setText(tuwen);
                        turangshidu.setText(tushi);
                        huanjingshidu.setText(huanshi);
                        guangqiang.setText(guangq);
                        huanjingwendu.setText(huanwen);
                        dianliu.setText(dianl);
                        Log.d("Handler", "传感器信息已更新");
                        break;
                    case 1:
                        Toast.makeText(WifiActivity.this, (String) (msg.obj), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };


        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0: {
                        turangwendu.setText(tuwen);
                        turangshidu.setText(tushi);
                        huanjingshidu.setText(huanshi);
                        guangqiang.setText(guangq);
                        huanjingwendu.setText(huanwen);
                        dianliu.setText(dianl);
                        break;
                    }
                }

            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(1, 1, 1, "设备状态");
        menu.add(1, 2, 1, "数据存储");
        menu.add(1, 3, 1, "返回");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {   //点击返回后返回设备状态位
        switch (item.getItemId()) {
            case 1:
                //finish();
                Intent intent = new Intent();
                intent.setClass(WifiActivity.this, ControlActivity.class);
               /*
                Bundle bundle = new Bundle();
                bundle.putString("waizheyang_state", waizheyang_state);
                bundle.putString("neizheyang_state", neizheyang_state);
                bundle.putString("dingkaichuang_state", dingkaichuang_state);
                bundle.putString("cekaichuang_state", cekaichuang_state);
                bundle.putString("shilian_state", shilian_state);
                bundle.putString("zhouliufengji_state", zhouliufengji_state);
                bundle.putString("huanliufengji_state", huanliufengji_state);
                bundle.putString("wuhua_state", wuhua_state);
                bundle.putString("guangai_state", guangai_state);
                intent.putExtras(bundle);
                */
                startActivity(intent);
                break;
            case 2:
                Intent intent1 = new Intent();
                intent1.setClass(WifiActivity.this, displaydata.class);
                startActivity(intent1);
                break;
            case 3:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        setContentView(R.layout.activity_wifi);

        setTitle("温室监控系统");

        turangwendu = findViewById(R.id.turangwendu);
        turangshidu = findViewById(R.id.turangshidu);
        huanjingshidu = findViewById(R.id.huanjingshidu);
        guangqiang = findViewById(R.id.guangqiang);
        huanjingwendu = findViewById(R.id.huanjingwendu);
        dianliu = findViewById(R.id.dianliu);

        turangwenduChart = findViewById(R.id.turangwenduChart);
        turangshiduChart = findViewById(R.id.turangshiduChart);
        huanjingshiduChart = findViewById(R.id.huanjingshiduChart);
        guangqiangChart = findViewById(R.id.guangqiangChart);
        huanjingwenduChart = findViewById(R.id.huanjingwenduChart);
        dianliuChart = findViewById(R.id.dianliuChart);

    }

    private void initChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setDrawBorders(true);
        chart.setBorderColor(0xA2A2A2);
        LineData data = new LineData();
        chart.setData(data);
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setEnabled(true);

        XAxis xl = chart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawLabels(false);
        xl.setGranularity(1f);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.enableGridDashedLine(10f, 10f, 0f);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        //leftAxis.setTextColor(Color.parseColor("#A2A2A2"));
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.resetAxisMinimum();
        leftAxis.resetAxisMaximum();

        leftAxis.setDrawGridLines(true);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void addEntry(LineChart chart, String name, String color, float number) {

        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createLineDataSet(name, color);
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), number), 0);
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(30);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createLineDataSet(String name, String color) {

        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.parseColor(color));
        set.setCircleColor(Color.WHITE);
//        set.setLineWidth(2f);
//        set.setCircleRadius(3f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }




    private void netDataReceived(char[] temp) {

        /*
        jiedianwei=new String(temp, 0, 1);
        jiedianwei_int=Integer.parseInt(jiedianwei);
        if (jiedianwei_int == 1)
        {
            huanwen = new String(temp, 1, 2);
            huanshi = new String(temp, 3, 2);
        }
        else if(jiedianwei_int == 2)
        {
            tuwen = new String(temp, 1, 2);
            tushi = new String(temp, 3, 2);
        }
        else if (jiedianwei_int == 3)
        {
            int gqchangdu = 0;
            String s = new String(temp);
            gqchangdu=s.length();
            if (gqchangdu == 5) {
                guangq = new String(temp, 1, 4);
            }else if(gqchangdu == 4)
            {
                guangq = new String(temp, 1, 3);
            }

        }
        else if (jiedianwei_int == 4)
        {
            shilian_state = new String(temp, 1, 1);
            zhouliufengji_state = new String(temp, 1, 1);
            huanliufengji_state = new String(temp, 1, 1);
        }
        else if (jiedianwei_int == 5)
        {
            wuhua_state = new String(temp, 1, 1);
            guangai_state = new String(temp, 1, 1);
        }
        else if (jiedianwei_int == 6)
        {
            dingkaichuang_state = new String(temp, 1, 1);
            cekaichuang_state = new String(temp, 1, 1);
        }
        else if (jiedianwei_int == 7)
        {
            waizheyang_state = new String(temp, 1, 1);
            neizheyang_state = new String(temp, 1, 1);
        }
        */






        tuwen = new String(temp, 1, 2);
        tushi = new String(temp, 3, 2);
        huanwen = new String(temp, 5, 2);
        huanshi = new String(temp, 7, 2);
        guangq = new String(temp, 9, 4);

        dianl = new String(temp, 13, 2);
        //接收到的状态位
        waizheyang_state = new String(temp, 15, 1);
        neizheyang_state = new String(temp, 16, 1);
        dingkaichuang_state = new String(temp, 17, 1);
        cekaichuang_state = new String(temp, 18, 1);
        shilian_state = new String(temp, 19, 1);
        zhouliufengji_state = new String(temp, 20, 1);
        huanliufengji_state = new String(temp, 21, 1);
        wuhua_state = new String(temp, 22, 1);
        guangai_state = new String(temp, 23, 1);

        //结束
        turangwendu.setText(tuwen);
        turangshidu.setText(tushi);
        huanjingshidu.setText(huanshi);
        guangqiang.setText(guangq);
        huanjingwendu.setText(huanwen);
        dianliu.setText(dianl);

        //转换为int型
        tuwen1=Integer.parseInt(tuwen);
        tushi1=Integer.parseInt(tushi);
        huanshi1=Integer.parseInt(huanshi);
        guangq1=Integer.parseInt(guangq);
        huanwen1=Integer.parseInt(huanwen);
        dianl1=Integer.parseInt(dianl);
        addEntry(turangwenduChart, "土壤温度", TDS_LINE_COLOR, tuwen1);
        addEntry(turangshiduChart, "土壤湿度", TEMP_LINE_COLOR, tushi1);
        addEntry(huanjingshiduChart, "环境湿度", HUM_LINE_COLOR, huanshi1);
        addEntry(guangqiangChart, "光强", LIGHT_LINE_COLOR, guangq1);
        addEntry(huanjingwenduChart, "环境温度", TDS_LINE_COLOR, huanwen1);
        addEntry(dianliuChart, "电流", HUM_LINE_COLOR, dianl1);
        //报警
        //if(dianl1==0){
            //停电报警
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            builder.setContentTitle("标题：警告");
            builder.setContentText("内容：温室停电！");
            builder.setShowWhen(true);
            Date dt= new Date();
            builder.setAutoCancel(true); //设置点击通知后，状态栏自动删除通知。
            //下面创建 Notification对象
            Notification notification = builder.build();//调用builder.build()，可获得Notification的对象
            manager.notify(SIMPLE_NOTFICATION_ID, notification);
            //报警模块结束
      //  }

        //数据库操作
        SQLiteDatabase dbWriter = dbOpenHelper.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("device", shebei);
        cv.put("trwendu", tuwen1);
        cv.put("trshidu", tushi1);
        cv.put("hwendu", huanwen1);
        cv.put("hshidu", huanshi1);
        cv.put("guangq", guangq1);
        cv.put("dianliu", dianl1);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        shijian=year+"/"+month+"/"+day+"  "+hour+":"+minute+":"+second;
        cv.put("shijian", shijian);
        dbWriter.insert("wenshi", null, cv); //向数据表中追加数据，第二个参数是对空列的填充处理策略
    }

    private void sendHandlerMessage(int what, Object msg) {
        Message message = new Message();
        message.what = what;
        message.obj = msg;
        handler.sendMessage(message);
    }
    private void tcpStart() {

        //与电脑建立TCP连接

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //socket = new Socket("192.168.43.210", 3334);
                    socket = new Socket(pcIp, pcport);
                    sendHandlerMessage(1, "连接成功！");
                    //获取电脑输出流
                    pcTcpOutputStream = socket.getOutputStream();
                    // 创建输入流对象InputStream
                    is = socket.getInputStream();
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    response = br.readLine();
                                    char[] temp = response.toCharArray();
                                    netDataReceived(temp);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                    Message msg = Message.obtain();
                    msg.what = 0;
                    mMainHandler.sendMessage(msg);

                } catch (Exception e) {
                    sendHandlerMessage(1, "连接失败！");
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbWriter.close();

    }
}
