package com.camnter.newlife.views.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.camnter.newlife.R;
import com.camnter.newlife.core.BaseAppCompatActivity;
import com.camnter.newlife.utils.DeviceUtils;
import java.util.List;

/**
 * Description：SensorManagerActivity
 * Created by：CaMnter
 * Time：2015-10-27 15:54
 */
public class SensorManagerActivity extends BaseAppCompatActivity {
    private TextView sensorManagerTV;
    private LinearLayout rootLayout;


    /**
     * Fill in layout id
     *
     * @return layout id
     */
    @Override protected int getLayoutId() {
        return R.layout.activity_sensor_manager;
    }


    /**
     * Initialize the view in the layout
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override protected void initViews(Bundle savedInstanceState) {
        this.rootLayout = (LinearLayout) this.findViewById(R.id.sensor_root_layout);
        this.sensorManagerTV = (TextView) this.findViewById(R.id.sensor_count_tv);
    }


    /**
     * Initialize the View of the listener
     */
    @Override protected void initListeners() {

    }


    /**
     * Initialize the Activity data
     */
    @Override protected void initData() {
        // 获取传感器管理器
        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

        // 获取默认加速度传感器
        Sensor linear = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        /**
         * 传感器监听
         * SensorEvent.values[0] = x
         * SensorEvent.values[1] = y
         * SensorEvent.values[2] = z
         */
        SensorEventListener listener = new SensorEventListener() {
            @Override public void onSensorChanged(SensorEvent event) {
                String info = "";
                for (int i = 0; i < event.values.length; i++) {
                    info += "event.values[" + i + "]：" + event.values[i] + "\t";
                }
                Log.i("SensorManagerActivity", info);
            }


            @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        // 给对应传感器添加监听
        sensorManager.registerListener(listener, linear, SensorManager.SENSOR_DELAY_NORMAL);

        // 获得全部的传感器列表
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        this.sensorManagerTV.setText(sensors.size() + "");
        for (Sensor sensor : sensors) {
            TextView title = new TextView(this);
            title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            title.setTextSize(15);
            title.setTextColor(this.getResources().getColor(R.color.themeColor));
            switch (sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER: {
                    title.setText("1.加速度传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_MAGNETIC_FIELD: {
                    title.setText("2.磁力传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_ORIENTATION: {
                    title.setText("3.方向传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_GYROSCOPE: {
                    title.setText("4.陀螺仪传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_LIGHT: {
                    title.setText("5.光线感应传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_PRESSURE: {
                    title.setText("6.压力传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_TEMPERATURE: {
                    title.setText("7.温度传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_PROXIMITY: {
                    title.setText("8.距离传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_GRAVITY: {
                    title.setText("9.重力传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_LINEAR_ACCELERATION: {
                    title.setText("10.线性加速度传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
                case Sensor.TYPE_ROTATION_VECTOR: {
                    title.setText("11.旋转矢量传感器" + "（" + sensor.getName() + "）");
                    this.addInfoView(sensor, title);
                    break;
                }
            }
        }
    }


    private void addInfoView(Sensor sensor, TextView title) {
        String tempString = "\n" + "设备版本：" + sensor.getVersion() + "\n" + "供应商：" +
                sensor.getVendor() + "\n" + "最大取值范围：" + sensor.getMaximumRange() + "\n功率：" +
                sensor.getPower() + "\n精度：" + sensor.getResolution() + "\n传感器类型：" +
                sensor.getType();

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int paddingPX = DeviceUtils.dp2px(this, 6);
        linearLayout.setPadding(paddingPX, paddingPX, paddingPX, paddingPX);
        linearLayout.setLayoutParams(params);

        linearLayout.addView(title);

        TextView content = new TextView(this);
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        content.setTextSize(15);
        content.setTextColor(this.getResources().getColor(R.color.colorAccent));
        content.setText(tempString);
        linearLayout.addView(content);

        this.rootLayout.addView(linearLayout);
    }
}
