package com.example.wenshi;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControlActivity extends AppCompatActivity {
    private Switch waizheyangshang,waizheyangxia,dingkaichuang,cekaichuang,shilianfengji,zhouliufengji,huanliufengji,wuhuaxitong,guangaixitong;
    String waizheyang_state="1",dingkaichuang_state="1",cekaichuang_state="1",shilian_state="1",zhouliufengji_state="1";
    String neizheyang_state="1",jiedianwei="0";
    String huanliufengji_state="1",wuhua_state="1",guangai_state="1";
    String waizheyangshang_order="0",waizheyangxia_order="0",dingkaichuang_order="0",cekaichuang_order="0",shilianfengji_order="0";
    String zhouliufengji_order="0",huanliufengji_order="0",wuhuaxitong_order="0",guangaixitong_order="0";
    String wendushang,wenduxia;
    String response="#00000000000000000000000";
    int jiedianwei_int;
    private EditText temperaturelow,temperaturehigh;
    private TextView waizheyangshang_state_tx,waizheyangxia_state_tx,dingkaichuang_statetx,cekaichuang_state_tx;
    private TextView shilianfengji_state_tx,zhouliufengji_state_tx,huanliufengji_state_tx,wuhuaxitong_state_tx,guangaixitong_state_tx;
    private Button sendbutton;
    OutputStream outputStream;
    private Handler handler;
    private Handler mMainHandler;
    private Socket socket;
    private OutputStream pcTcpOutputStream;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    // 线程池
    private ExecutorService mThreadPool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        initUI();
        /*
        //接收状态位
        Intent intent = getIntent();
        Bundle bundle =intent.getExtras();
        waizheyang_state = bundle.getString("waizheyang_state");
        neizheyang_state=bundle.getString("neizheyang_state");
        dingkaichuang_state=bundle.getString("dingkaichuang_state");
        cekaichuang_state=bundle.getString("cekaichuang_state");
        shilian_state=bundle.getString("shilian_state");
        zhouliufengji_state=bundle.getString("zhouliufengji_state");
        huanliufengji_state=bundle.getString("huanliufengji_state");
        wuhua_state=bundle.getString("wuhua_state");
        guangai_state=bundle.getString("guangai_state");
        //结束
        */




        //发送按钮
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchbutton();
                wendushang=temperaturehigh.getText().toString().trim();
                wenduxia=temperaturelow.getText().toString().trim();
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        // 从Socket 获得输出流对象OutputStream
                        // 该对象作用：发送数据
                        try {
                            outputStream = socket.getOutputStream();
                            // outputStream.write(("#XM"+inmingzi+"BJ"+inbanji+"XH"+inxuehao+"LH"+louhao+"YY"+shijian+"SJ"+stryear+"/"+strmonth+"/"+strday+"/"+strhour+":"+strminute).getBytes("UTF-8"));
                           /*
                            outputStream.write(("#"+wenduxia+wendushang+waizheyangshang_order+waizheyangxia_order+dingkaichuang_order+cekaichuang_order+shilianfengji_order
                            +zhouliufengji_order+huanliufengji_order+wuhuaxitong_order+guangaixitong_order+"1#").getBytes("GB2312"));
                            */
                            outputStream.write(("#"+wenduxia+wendushang+guangaixitong_order+dingkaichuang_order+shilianfengji_order
                                    +wuhuaxitong_order+waizheyangshang_order+"1#").getBytes("GB2312"));
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });



        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //updateSensorUI();
                        Log.d("Handler", "传感器信息已更新");
                        break;
                    case 1:
                        Toast.makeText(ControlActivity.this, (String) (msg.obj), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        mThreadPool = Executors.newCachedThreadPool();
        tcpStart();






    }


    private void tcpStart() {

        //与电脑建立TCP连接

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket("192.168.43.210", 3334);

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

    private void netDataReceived(char[] temp) {
        /*
        //接收到的状态位
        jiedianwei=new String(temp, 0, 1);
        jiedianwei_int=Integer.parseInt(jiedianwei);
        if (jiedianwei_int == 4)
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


        if (waizheyang_state.equals("1")){
            waizheyangshang_state_tx.setText("关");
        }else if(waizheyang_state.equals("0")){
            waizheyangshang_state_tx.setText("开");
        }
        if (neizheyang_state.equals("1")){
            waizheyangxia_state_tx.setText("关");
        }else if(neizheyang_state.equals("0")){
            waizheyangxia_state_tx.setText("开");
        }
        if (dingkaichuang_state.equals("1")){
            dingkaichuang_statetx.setText("关");
        }else if(dingkaichuang_state.equals("0")){
            dingkaichuang_statetx.setText("开");
        }
        if (cekaichuang_state.equals("1")){
            cekaichuang_state_tx.setText("关");
        }else if(cekaichuang_state.equals("0")){
            cekaichuang_state_tx.setText("开");
        }
        if (shilian_state.equals("1")){
            shilianfengji_state_tx.setText("关");
        }else if(shilian_state.equals("0")){
            shilianfengji_state_tx.setText("开");
        }
        if (zhouliufengji_state.equals("1")){
            zhouliufengji_state_tx.setText("关");
        }else if(zhouliufengji_state.equals("0")){
            zhouliufengji_state_tx.setText("开");
        }
        if (huanliufengji_state.equals("1")){
            huanliufengji_state_tx.setText("关");
        }else if(huanliufengji_state.equals("0")){
            huanliufengji_state_tx.setText("开");
        }
        if (wuhua_state.equals("1")){
            wuhuaxitong_state_tx.setText("关");
        }else if(wuhua_state.equals("0")){
            wuhuaxitong_state_tx.setText("开");
        }
        if (guangai_state.equals("1")){
            guangaixitong_state_tx.setText("关");
        }else if(guangai_state.equals("0")){
            guangaixitong_state_tx.setText("开");
        }



    }




    private void initUI() {
        waizheyangshang=findViewById(R.id.waizheyangshang);
        //waizheyangxia=findViewById(R.id.waizheyangxia);
        dingkaichuang=findViewById(R.id.dingkaichuang);
       // cekaichuang=findViewById(R.id.cekaichuang);
        shilianfengji=findViewById(R.id.shilianfengji);
        //zhouliufengji=findViewById(R.id.zhouliufengji);
       // huanliufengji=findViewById(R.id.huanliufengji);
        wuhuaxitong=findViewById(R.id.wuhuaxitong);
        guangaixitong=findViewById(R.id.guangaixitong);
        sendbutton=findViewById(R.id.sendbutton);
        temperaturelow = (EditText)findViewById(R.id.temperaturelow);
        temperaturehigh = (EditText)findViewById(R.id.temperaturehigh);
        waizheyangshang_state_tx = (TextView)findViewById(R.id.waizheyangshang_state);
        waizheyangxia_state_tx = (TextView)findViewById(R.id.waizheyangxia_state);
        dingkaichuang_statetx = (TextView)findViewById(R.id.dingkaichuang_state);
        cekaichuang_state_tx = (TextView)findViewById(R.id.cekaichuang_state);
        shilianfengji_state_tx = (TextView)findViewById(R.id.shilianfengji_state);
        zhouliufengji_state_tx = (TextView)findViewById(R.id.zhouliufengji_state);
        huanliufengji_state_tx = (TextView)findViewById(R.id.huanliufengji_state);
        wuhuaxitong_state_tx = (TextView)findViewById(R.id.wuhuaxitong_state);
        guangaixitong_state_tx = (TextView)findViewById(R.id.guangaixitong_state);
    }
    //开关按钮初始化
    private  void switchbutton(){
        if(guangaixitong.isChecked()==true){
            guangaixitong_order="0";
        }else{
            guangaixitong_order="1";
        }
        if(dingkaichuang.isChecked()==true){
            dingkaichuang_order="1";
        }else{
            dingkaichuang_order="0";
        }
        if(shilianfengji.isChecked()==true){
            shilianfengji_order="0";
        }else{
            shilianfengji_order="1";
        }
        if(wuhuaxitong.isChecked()==true){
            wuhuaxitong_order="0";
        }else{
            wuhuaxitong_order="1";
        }

        if(waizheyangshang.isChecked()==true){
            waizheyangshang_order="0";
        }else{
            waizheyangshang_order="1";
        }
        /*
        if(waizheyangxia.isChecked()==true){
            waizheyangxia_order="1";
        }else{
            waizheyangxia_order="0";
        }
        if(dingkaichuang.isChecked()==true){
            dingkaichuang_order="1";
        }else{
            dingkaichuang_order="0";
        }
        if(cekaichuang.isChecked()==true){
            cekaichuang_order="1";
        }else{
            cekaichuang_order="0";
        }
        if(shilianfengji.isChecked()==true){
            shilianfengji_order="1";
        }else{
            shilianfengji_order="0";
        }
        if(zhouliufengji.isChecked()==true){
            zhouliufengji_order="1";
        }else{
            zhouliufengji_order="0";
        }
        if(huanliufengji.isChecked()==true){
            huanliufengji_order="1";
        }else{
            huanliufengji_order="0";
        }
        if(wuhuaxitong.isChecked()==true){
            wuhuaxitong_order="1";
        }else{
            wuhuaxitong_order="0";
        }
        if(guangaixitong.isChecked()==true){
            guangaixitong_order="1";
        }else{
            guangaixitong_order="0";
        }
        */

    }



    private void sendHandlerMessage(int what, Object msg) {
        Message message = new Message();
        message.what = what;
        message.obj = msg;
        handler.sendMessage(message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(1, 1, 1, "返回");
        menu.add(1, 2, 1, "数据显示");
        menu.add(1, 3, 1, "数据存储");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                finish();
                break;
            case 2:
                Intent intent1 = new Intent();
                intent1.setClass(ControlActivity.this, WifiActivity.class);
                startActivity(intent1);
                break;
            case 3:
                Intent intent = new Intent();
                intent.setClass(ControlActivity.this, displaydata.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
