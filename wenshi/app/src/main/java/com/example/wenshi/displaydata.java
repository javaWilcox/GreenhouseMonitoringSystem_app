package com.example.wenshi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class displaydata extends AppCompatActivity {
    // 声明对数据库进行增删改查操作的DBAdapter类
    private SQLiteDatabase dbWriter,dbReader;
    private MyDBOpenHelper dbOpenHelper;
    private SimpleCursorAdapter listViewAdapter;
    private ListView listView;
    private String currentID="1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displaydata);
        listView = (ListView) findViewById(R.id.listView);
        dbOpenHelper = new MyDBOpenHelper(getApplicationContext(), "SC_Database.db", null, 1);
        dbReader = dbOpenHelper.getReadableDatabase();
        dbWriter = dbOpenHelper.getWritableDatabase();
        showAll();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击项：
                //ListView listView = (ListView) parent;
                //HashMap<String, Object> data = (HashMap<String, Object>) listView.getItemAtPosition(position);
                TextView itemID = (TextView) view.findViewById(R.id.item_id);
                TextView device = (TextView) view.findViewById(R.id.device);
                TextView trwendu = (TextView) view.findViewById(R.id.trwendu);
                TextView trshidu = (TextView) view.findViewById(R.id.trshidu);
                TextView hwendu = (TextView) view.findViewById(R.id.hwendu);
                TextView hshidu = (TextView) view.findViewById(R.id.hshidu);
                TextView guangq = (TextView) view.findViewById(R.id.guangq);
                TextView dianliu = (TextView) view.findViewById(R.id.dianliu);
                TextView shijian = (TextView) view.findViewById(R.id.shijian);
                //currentID =itemID.getText().toString();
                //Log.d("我的提示", "_id----"+currentID);
            }
        });
/*
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView itemID = (TextView) view.findViewById(R.id.item_id);
                currentID =itemID.getText().toString();
                dbWriter.delete("student","_id=?", new String[]{currentID});
                showAll();
                return true;
            }
        });
*/


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(1, 1, 1, "返回");
        menu.add(1, 2, 1, "删除当前数据");
        menu.add(1, 3, 1, "数据显示");
        menu.add(1, 4, 1, "设备状态");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                finish();
                break;
            case 2:
                dbWriter.delete("wenshi", null, null);
                showAll();
                break;
            case 3:
                Intent intent1 = new Intent();
                intent1.setClass(displaydata.this, WifiActivity.class);
                startActivity(intent1);
                break;
            case 4:
                Intent intent = new Intent();
                intent.setClass(displaydata.this, ControlActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showAll() {
        Cursor result = dbReader.query("wenshi", null, null, null, null, null, "_id", null);
        if (!result.moveToFirst()) { //判断游标是否为空
            Toast.makeText(getApplicationContext(), "数据表中一个数据也没有！", Toast.LENGTH_LONG).show();
        }
        listViewAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.item_list, result,
                new String[]{"_id","device", "trwendu", "trshidu","hwendu","hshidu","guangq","dianliu","shijian"},
                new int[]{R.id.item_id,R.id.device, R.id.trwendu, R.id.trshidu,R.id.hwendu,R.id.hshidu,R.id.guangq,R.id.dianliu,R.id.shijian},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(listViewAdapter);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbReader.close();
    }
}
