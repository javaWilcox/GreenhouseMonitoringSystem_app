package com.example.wenshi;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private EditText edtPcIp;
    private EditText edtPcPort;

    private String pcIp;
    private String pcPort;
    private String mcuIP="127.0.0.1";
    private String mcuPort="3333";
    private String protocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtPcIp = (EditText)findViewById(R.id.edtPcIp);
        edtPcPort = (EditText)findViewById(R.id.edtPcPort);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);           //工具栏
        setSupportActionBar(toolbar);

   /***************************/
   //小信封，功能：用于发送信息
   /***************************/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);      //定义的抽屉布局
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                  //右上角菜单
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();                                 //导入抽屉里的各项菜单

        if (id == R.id.nav_gallery) {                            //数据显示
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, WifiActivity.class);
            pcIp = edtPcIp.getText().toString().trim();
            pcPort = edtPcPort.getText().toString().trim();
            Bundle bundle = new Bundle();
            bundle.putString("PC_IP", pcIp);
            bundle.putString("PC_PORT", pcPort);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {                  //历史数据
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, displaydata.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {                     //远程控制
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ControlActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);  //关闭抽屉
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
