package com.example.yzc.collect.base;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;


import com.example.yzc.collect.treelist.Node;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private SensorManager sensor;
    protected List<Node> mDatas = new ArrayList<Node>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatas();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initDatas()
    {
        mDatas.add(new Node(0+"", "-1", "动作传感器"));
        mDatas.add(new Node(1+"", "-1", "环境传感器"));
        mDatas.add(new Node(2+"", "-1", "位置传感器"));
        sensor=(SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> list=sensor.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sen:list){
            switch (sen.getType()) {
                case Sensor.TYPE_ACCELEROMETER://加速度传感器
                    mDatas.add(new Node(3+"", "0", "加速度传感器"));
//                    mDatas.add(new Node(11+"", 1+"", "加速度传感器的X轴数据"));
//                    mDatas.add(new Node(12+"", 1+"", "加速度传感器的Y轴数据"));
//                    mDatas.add(new Node(13+"", 1+"", "加速度传感器的Z轴数据"));
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    mDatas.add(new Node(4+"", "0", "陀螺仪传感器"));
//                    mDatas.add(new Node(41+"", 4+"", "陀螺仪传感器的X轴数据"));
//                    mDatas.add(new Node(42+"", 4+"", "陀螺仪传感器的Y轴数据"));
//                    mDatas.add(new Node(43+"", 4+"", "陀螺仪传感器的Z轴数据"));
                    break;
                case Sensor.TYPE_GRAVITY:
                    mDatas.add(new Node(5+"", "0", "重力传感器"));
//                    mDatas.add(new Node(31+"", 3+"", "重力传感器的X轴数据"));
//                    mDatas.add(new Node(32+"", 3+"", "重力传感器的Y轴数据"));
//                    mDatas.add(new Node(33+"", 3+"", "重力传感器的Z轴数据"));
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    mDatas.add(new Node(6+"", "0", "线性加速度传感器"));
//                    mDatas.add(new Node(71+"", 7+"", "线性加速度传感器的X轴数据"));
//                    mDatas.add(new Node(72+"", 7+"", "线性加速度传感器的Y轴数据"));
//                    mDatas.add(new Node(73+"", 7+"", "线性加速度传感器的Z轴数据"));
                    break;

                case Sensor.TYPE_LIGHT:
                    mDatas.add(new Node(7+"", "1", "光线传感线"));
//                    mDatas.add(new Node(51+"", 5+"", "设备周围光的强度（勒克斯）"));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mDatas.add(new Node(8+"", "2", "磁场传感器"));
//                    mDatas.add(new Node(61+"", 6+"", "磁场传感器的X轴数据"));
//                    mDatas.add(new Node(62+"", 6+"", "磁场传感器的Y轴数据"));
//                    mDatas.add(new Node(63+"", 6+"", "磁场传感器的Z轴数据"));
                    break;
                case Sensor.TYPE_PRESSURE:
                    mDatas.add(new Node(9+"", "1", "压力传感器"));
                    break;
                case Sensor.TYPE_ORIENTATION:
                    mDatas.add(new Node(10+"", "0", "方向传感器"));
                    break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    mDatas.add(new Node(11+"", "1", "温度传感器"));
                    break;
                default:
                    break;
            }
        }
    }
}
