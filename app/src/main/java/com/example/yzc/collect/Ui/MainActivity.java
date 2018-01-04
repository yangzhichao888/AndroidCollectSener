package com.example.yzc.collect.Ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yzc.collect.CollectService;
import com.example.yzc.collect.R;
import com.example.yzc.collect.adapter.SimpleTreeAdapter;
import com.example.yzc.collect.base.BaseActivity;
import com.example.yzc.collect.treelist.Node;
import com.example.yzc.collect.treelist.TreeListViewAdapter;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {

    private TextView tv_state, tv_mail, tv_finish;
    private MyReceiver receiver;
    private Button b;
    private TextView banner;

    String[] sensorname = new String[30];
    Intent intent;
    CollectService service;
    ServiceConnection conn = new ServiceConnection() {
        @Override//当启动源跟Servic成功连接会调用
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //取返回的对象
            service = ((CollectService.MyBinder)iBinder).getService();
        }
        @Override//当启动源跟Service的连接意外丢失的时候会调用
        //比如Service崩溃了或者被强行杀死
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private TreeListViewAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView mTree = (ListView) findViewById(R.id.lv_tree);
        tv_state = (TextView) findViewById(R.id.tv_userstate);
        tv_mail = (TextView) findViewById(R.id.tv_emailstate);
        tv_finish = (TextView) findViewById(R.id.tv_finish);
        b = (Button) findViewById(R.id.tv_button);
        banner = (TextView) findViewById(R.id.banner);

        banner.setText("\n本APP打开后将直接进入后台运行。\n" +
                "***** 请不要用摇晃手机的方法 ***\n" +
                "本程序只收集用户运动状态下手机传感器的数据，不会泄漏您的隐私。\n");

        b.setEnabled(false);
        //第一个参数  ListView
        //第二个参数  上下文
        //第三个参数  数据集
        //第四个参数  默认展开层级数 0为不展开
        //第五个参数  展开的图标
        //第六个参数  闭合的图标
         mAdapter = new SimpleTreeAdapter(mTree, MainActivity.this,
                        mDatas, 0,R.mipmap.tree_ex,R.mipmap.tree_ec);
         mTree.setAdapter(mAdapter);
        final List<Node> allNodes = mAdapter.getAllNodes();

        Arrays.fill(sensorname, "0");

        //注册广播接收器
        receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.yzc.collect.Collectservice");
        MainActivity.this.registerReceiver(receiver,filter);
        tv_state.setText(receiver.getUserState());
        tv_mail.setText(receiver.getEmailState());
        tv_finish.setText(receiver.getFinish());
        Run();
    }

    private void Run(){
        StringBuilder sb = new StringBuilder();
        //获取排序过的nodes
        //如果不需要刻意直接用 mDatas既可
        final List<Node> allNodes = mAdapter.getAllNodes();

        for (int i = 0; i < allNodes.size(); i++) {
            if (allNodes.get(i).isChecked()){
                sensorname[i] = allNodes.get(i).getName();
                Log.i("info",sensorname[i]);
            }
        }
        Bundle bundle = new Bundle() ;
        bundle.putSerializable("DATA", sensorname) ;
        //在这里启动Service
        intent = new Intent(MainActivity.this,CollectService.class);
        intent.putExtras(bundle);
        startService(intent);
        bindService(intent,conn, Service.BIND_AUTO_CREATE);

        String strNodesName = sb.toString();
        if (!TextUtils.isEmpty(strNodesName))
            Toast.makeText(this, strNodesName.substring(0, strNodesName.length()-1), Toast.LENGTH_SHORT).show();
    }
    /**
     * 显示选中数据
     * 这里是按钮控制，也可以定时器控制
     */
    public void clickShow(View v) {
        Toast.makeText(this, "传感器数据收集程序和服务退出", Toast.LENGTH_SHORT).show();
        System.exit(0);
    }


    private class MyReceiver extends BroadcastReceiver {
        private String UserState;       //用户的行为状态
        private String EmailState;      //发送邮件的状态
        private String Finish;           //发送结束

        public String getUserState(){
            return UserState;
        }
        public String getEmailState(){
            return EmailState;
        }
        public String getFinish(){
            return Finish;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String us = bundle.getString("UserState");
            if(us!=null) {
                UserState = us;
                tv_state.setText(UserState);
            }

            String ms = bundle.getString("EmailState");
            if(ms!=null) {
                EmailState = ms;
                tv_mail.setText(EmailState);
            }

            String fin = bundle.getString("Finish");
            if(fin!=null){
                Finish = fin;
                tv_finish.setText(Finish);
                b.setText("任务完成，点击退出");
                b.setEnabled(true);
             }
        }
    }
    @Override
    protected void onDestroy() {
        stopService(intent);
        unbindService(conn);
        super.onDestroy();
    }

}
