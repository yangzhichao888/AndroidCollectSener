package com.example.yzc.collect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.yzc.collect.sendfile.SendEmail;

import java.util.List;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_GRAVITY;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_ORIENTATION;
import static android.hardware.Sensor.TYPE_PRESSURE;

/**
 * Created by yzc on 2017/11/2.
 */

public class CollectService extends Service implements SensorEventListener{

    String[] recvData = new String[30];
    StringBuffer sb = new StringBuffer();
    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();
    StringBuffer sb4 = new StringBuffer();
    StringBuffer sb5 = new StringBuffer();
    StringBuffer sb6 = new StringBuffer();
    StringBuffer sb7 = new StringBuffer();
    StringBuffer sb8 = new StringBuffer();
    StringBuffer sb9 = new StringBuffer();

    private int nu = 200;
    Intent intent;
    int count = 0;//计步数
    int emailNum = 0;//发送邮件数
    int num1=0,num2=0,num3=0,num4=0,num5=0,num6=0,num7=0,num8=0,num9=0;
    int  sb_length;
    //存放三轴数据
    double[] oriValues = new double[3];
    final int valueNum = 4;
    //是否上升的标志位
    boolean isDirectionUp = false;
    //持续上升次数
    int continueUpCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降
    boolean lastStatus = false;
    //波峰值
    double peakOfWave = 0;
    //波谷值
    double valleyOfWave = 0;
    //此次波峰的时间
    long timeOfThisPeak = 0;
    //上次波峰的时间
    long timeOfLastPeak = 0;
    //当前的时间
    long timeOfNow = 0;
    //当前传感器的值
    double gravityNew = 0;
    //上次传感器的值
    double gravityOld = 0;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    final float initialValue = (float) 1.3;
    //初始阈值
    double ThreadValue = (float) 2.0;
    //定义系统的Sensor管理器
    SensorManager sensorManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Data(sensorEvent);//收集其他传感器的数据
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //三维数据的初步筛选
            for (int i = 0; i < 3; i++) {
                oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
            }
            gravityNew = Calculate.calculate(oriValues[0], oriValues[1], oriValues[2]);//开平方根，得出一个向量似的值
            if (gravityOld == 0) {//上次传感器数据为0，则赋新值
                gravityOld = gravityNew;
            } else {
                if (DetectorPeak(gravityNew, gravityOld)) {
                    timeOfLastPeak = timeOfThisPeak;//此次波峰变成上次波峰
                    timeOfNow = System.currentTimeMillis();//获得此次波峰的时间
                    if (timeOfNow - timeOfLastPeak >= 250//如果时间大于250毫秒
                            && (peakOfWave - valleyOfWave >= initialValue)) {//并且阙值大于初始的阙值
                        timeOfThisPeak = timeOfNow;//认为走了一步，改变时间，并记录数据
                        count++;//步数+1
                     /*
                     * 更新界面的处理，不涉及到算法
                     * 一般在通知更新界面之前，增加下面处理，为了处理无效运动：
                     * 1.连续记录10才开始计步
                     * 2.例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
                     * 3.连续记录了9步用户还在运动，之前的数据才有效
                     * */
                        if (count >= 10) {//如果步数>10
                            //发送广播,用来显示在界面上
                            intent = new Intent();
                            intent.putExtra("UserState", "行走状态, 请继续保持正常步行姿态。");
                            intent.setAction("com.example.yzc.collect.Collectservice");
                            sendBroadcast(intent);
                            if (num1 >= nu && num2>=nu && num3>=nu && num4 >=nu  && num6 >=nu
                                    && num8 >=nu || num9 >=nu) {  //收集 至少 100条数据,环境传感器除外
                                for (int i = 0;i < recvData.length;i++){
                                    if (recvData[i].equals("加速度传感器")){
                                        sb = sb.append("TYPE_ACCELEROMETER:" + sb1 + "\n");
                                    }
                                    if (recvData[i].equals("陀螺仪传感器")){
                                        sb = sb.append("TYPE_GYROSCOPE:" + sb2 + "\n");
                                    }
                                    if (recvData[i].equals("重力传感器")){
                                        sb = sb.append("TYPE_GRAVITY:" + sb3 + "\n");
                                    }
                                    if (recvData[i].equals("线性加速度传感器")){
                                        sb = sb.append("TYPE_LINEAR_ACCELERATION:" + sb4 + "\n");
                                    }
                                    if (recvData[i].equals("光线传感器")){
                                        sb = sb.append("TYPE_LIGHT:"  + sb5 + "\n");
                                    }
                                    if (recvData[i].equals("磁场传感器")){
                                        sb = sb.append("TYPE_MAGNETIC_FIELD:"  +  sb6 + "\n");
                                    }
                                    if (recvData[i].equals("温度传感器")){
                                        sb = sb.append("TYPE_AMBIENT_TEMPERATURE:"  +  sb7 + "\n");
                                    }
                                    if (recvData[i].equals("方向传感器")){
                                        sb = sb.append("TYPE_ORIENTATION:"  +  sb8 + "\n");
                                    }
                                    if (recvData[i].equals("压力传感器")){
                                        sb = sb.append("TYPE_PRESSURE:"  +  sb9 + "\n");
                                    }
                                }
                                //获取手机唯一标识符
                                String android_id = GetUniqueID.getUniqueID(this);
                                if(emailNum < 20) {
//                                    new SendEmail().execute(android_id, String.valueOf(sb));//发送邮件
                                    emailNum++;
//                                发送广播,用来显示在界面上
                                    intent = new Intent();
                                    intent.putExtra("EmailState", "发送第" + emailNum + "份邮件成功...");
                                }
                                else{
                                    intent = new Intent();
                                    intent.putExtra("Finish", "信息收集完成，共发送"+emailNum + "封邮件。");
                                }

                                //intent.putExtra("done", "DONE");
                                intent.setAction("com.example.yzc.collect.Collectservice");
                                sendBroadcast(intent);
//                                Log.i("email", "email: "+ android_id);
                                num1 = 0;num2=0;num3=0;num4=0;num5=0;num6=0;
                                deletestringbuffer(sb);
                                deletestringbuffer(sb1);
                                deletestringbuffer(sb2);
                                deletestringbuffer(sb3);
                                deletestringbuffer(sb4);
                                deletestringbuffer(sb5);
                                deletestringbuffer(sb6);
                                deletestringbuffer(sb7);
                                deletestringbuffer(sb8);
                                deletestringbuffer(sb9);
                            }
                        } else {//否则删掉之前记录的数据
                            num1 = 0;num2=0;num3=0;num4=0;num5=0;num6=0;num7=0;num8=0;num9=0;
                            deletestringbuffer(sb);
                            deletestringbuffer(sb1);
                            deletestringbuffer(sb2);
                            deletestringbuffer(sb3);
                            deletestringbuffer(sb4);
                            deletestringbuffer(sb5);
                            deletestringbuffer(sb6);
                            deletestringbuffer(sb7);
                            deletestringbuffer(sb8);
                            deletestringbuffer(sb9);
                        }
                    } else if (timeOfNow - timeOfLastPeak > 3000) { //3秒没走，除了发送邮件数，其余清0
                        count = 0;
                        num1 = 0;num2=0;num3=0;num4=0;num5=0;num6=0;
                        //发送广播,用来显示在界面上
                        intent = new Intent();
                        intent.putExtra("UserState", "停在原地, 请正常步行。");
                        intent.setAction("com.example.yzc.collect.Collectservice");
                        sendBroadcast(intent);
                    }
                }
                if (timeOfNow - timeOfLastPeak >= 250
                        && (peakOfWave - valleyOfWave >= initialValue)) {//initialValue动态数据的阈值
                    timeOfThisPeak = timeOfNow;
                }
                gravityOld = gravityNew;
            }
        }
    }

    private void deletestringbuffer(StringBuffer sb) {
            sb_length = sb.length();// 取得字符串的长度
            sb.delete(0, sb_length);    //删除字符串从0~sb_length-1处的内容 (这个方法就是用来清除StringBuffer中的内容的)
    }

    @Override
    public void onCreate() {
        //获取系统的传感器管理服务
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        super.onCreate();
    }
    private void Data(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()){

            case TYPE_ACCELEROMETER://加速度传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb1.append(oriValues[0] + ",");
                sb1.append(oriValues[1] + ",");
                sb1.append(oriValues[2] + ";");
                num1++;//收集一条，num加1
                break;
            case TYPE_GYROSCOPE://陀螺仪传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb2.append(oriValues[0] + ",");
                sb2.append(oriValues[1] + ",");
                sb2.append(oriValues[2] + ";");
                num2++;//收集一条，num加1
                break;
            case TYPE_GRAVITY://重力传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb3.append(oriValues[0] + ",");
                sb3.append(oriValues[1] + ",");
                sb3.append(oriValues[2] + ";");
                num3++;//收集一条，num加1
                break;
            case TYPE_LINEAR_ACCELERATION://线性加速度传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb4.append(oriValues[0] + ",");
                sb4.append(oriValues[1] + ",");
                sb4.append(oriValues[2] + ";");
                num4++;//收集一条，num加1
                break;
            case TYPE_LIGHT://光线传感线
                sb5.append(sensorEvent.values[0] + ";");
//                num5++;//收集一条，num加1
                break;
            case TYPE_MAGNETIC_FIELD://磁场传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb6.append(oriValues[0] + ",");
                sb6.append(oriValues[1] + ",");
                sb6.append(oriValues[2] + ";");
                num6++;//收集一条，num加1
                break;
            case TYPE_AMBIENT_TEMPERATURE://温度传感器
                    oriValues[0] = sensorEvent.values[0];//把三维数据取出来放入自定义的数组中
                    sb7.append(oriValues[0] + ";");
                    num7++;
                break;
            case TYPE_ORIENTATION://方向传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb8.append(oriValues[0] + ",");
                sb8.append(oriValues[1] + ",");
                sb8.append(oriValues[2] + ";");
                num8++;//收集一条，num加1
                break;
            case TYPE_PRESSURE://压力传感器
                for (int i = 0; i < 3; i++) {
                    oriValues[i] = sensorEvent.values[i];//把三维数据取出来放入自定义的数组中
                }
                sb9.append(oriValues[0] + ",");
                sb9.append(oriValues[1] + ",");
                sb9.append(oriValues[2] + ";");
                num9++;//收集一条，num加1
                break;
        }
    }

    private boolean DetectorPeak(double newValue, double oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 || oldValue >= 20)) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class MyBinder extends Binder{
        public CollectService getService(){
            return CollectService.this;
        }
    }

    //service被启动时回调该方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //为系统的线性加速度传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION),SensorManager.SENSOR_DELAY_GAME);
        // 为加速度传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        // 为重力加速度传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        // 为温度传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_GAME);
        // 为陀螺仪传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        // 为光线传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);
        // 为磁场传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        // 为方向传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        // 为压力传感器注册监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(TYPE_PRESSURE), SensorManager.SENSOR_DELAY_GAME);

        recvData = intent.getStringArrayExtra("DATA") ;//获得activity传来的数组，用于activity和service的交互
        return START_STICKY;//统有足够多资源的时候，就会重新开启service，保证后台运行
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener( this);
        emailNum = 0;//重置发送邮箱数
        super.onDestroy();
    }
}






