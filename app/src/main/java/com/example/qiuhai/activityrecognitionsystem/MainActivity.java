package com.example.qiuhai.activityrecognitionsystem;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button btn_start, btn_stop;
    private SensorManager sensorManager;
    Sensor accSensor, gyroSensor, magSensor;

    ArrayList<float[]> accBuffer;
    int bufferSize = 500;
    int calDelay;
    boolean isCal;
    int count;

    float[] acc;
    float[] gyro;
    float[] mag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        accBuffer = new ArrayList<>();
        count = 0;
        calDelay = 0;
        isCal = false;

        //get sensor manage
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



        btn_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                isCal = true;

                sensorManager.registerListener(MainActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(MainActivity.this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(MainActivity.this, magSensor, SensorManager.SENSOR_DELAY_GAME);

            }
        });

        btn_stop.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                isCal = false;
                sensorManager.unregisterListener(MainActivity.this);
            }
        });

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            acc = event.values;

            // control the calculation
            if(isCal) {
                //put the data into the buffer
                if(count < bufferSize) {
                    accBuffer.add(acc);
                } else {
                    accBuffer.remove(0);
                    accBuffer.add(acc);
                }
                count++;

                calDelay++;
                // every 50, calculate the measure once
                if(calDelay % 50 == 0) {





                }

            }


        }
        if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            gyro = event.values;
        }
        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
            mag = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }





    private void mean(ArrayList<float[]> input, float[] results) {
        if(input != null && input.size()>0 && input.get(0).length == 3) return;

        float[] sum = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            for(int i=0; i < input.size(); i++){
                sum[CH] += input.get(i)[CH];
            }
            results[CH] = sum[CH] / input.size();
        }

    }

    private void std(ArrayList<float[]> input, float[] mean, float[] results) {
        if(input != null && input.size()>0 && input.get(0).length == 3
                && mean != null && mean.length == 3) return;

        float[] sum = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            for(int i=0; i < input.size(); i++){
                sum[CH] += (input.get(i)[CH] - mean[CH]) * (input.get(i)[CH] - mean[CH]);
            }
            results[CH] = (float) Math.sqrt(sum[CH] / input.size());
        }

    }

    private void max(ArrayList<float[]> input, float[] results) {
        if(input != null && input.size()>0 && input.get(0).length == 3) return;

        float[] max = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            max[CH] = Integer.MIN_VALUE;
            for(int i=0; i < input.size(); i++){
                if(input.get(i)[CH] > max[CH]) {
                    max[CH] = input.get(i)[CH];
                }

            }
            results[CH] = max[CH];
        }

    }

    private void min(ArrayList<float[]> input, float[] results) {
        if(input != null && input.size()>0 && input.get(0).length == 3) return;

        float[] min = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            min[CH] = Integer.MAX_VALUE;
            for(int i=0; i < input.size(); i++){
                if(input.get(i)[CH] < min[CH]) {
                    min[CH] = input.get(i)[CH];
                }

            }
            results[CH] = min[CH];
        }

    }


}
