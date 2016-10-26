package com.example.qiuhai.activityrecognitionsystem;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_start, btn_stop;
    private SensorManager sensorManager;
    private MySensorEventListener sensorEventListener;
    Sensor acceSensor, gyroSensor, magSensor;

    float[] acc;
    float[] gyro;
    float[] mag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        sensorEventListener = new MySensorEventListener();
        //get sensor manage
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        acceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        btn_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {



            }
        });

    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, acceSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }


    private final class MySensorEventListener implements SensorEventListener
    {


        //可以得到传感器实时测量出来的变化值
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            //得到加速度的值
            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
            {
                acc = event.values;
            }
            if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
                gyro = event.values;
            }
            if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
                mag = event.values;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    }

}
