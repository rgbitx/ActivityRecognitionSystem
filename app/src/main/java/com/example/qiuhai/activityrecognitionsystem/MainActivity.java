package com.example.qiuhai.activityrecognitionsystem;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button btn_start, btn_stop;
    TextView tv_stand, tv_walk, tv_run, tv_cycle, tv_incar;
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

    ArrayList<Attribute> atts;
    ArrayList<String> classVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        tv_stand = (TextView) findViewById(R.id.tv_stand);
        tv_walk = (TextView) findViewById(R.id.tv_walk);
        tv_run = (TextView) findViewById(R.id.tv_run);
        tv_cycle = (TextView) findViewById(R.id.tv_cycle);
        tv_incar = (TextView) findViewById(R.id.tv_incar);

        accBuffer = new ArrayList<>();
        count = 0;
        calDelay = 0;
        isCal = false;

        // Model initialization
        atts = new ArrayList<>();
        classVal = new ArrayList<>();

        // set the attributions of the instance
        setInstanceAttributions(atts, classVal);


        //get sensor manage
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



        btn_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

//                sensorManager.registerListener(MainActivity.this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(MainActivity.this, magSensor, SensorManager.SENSOR_DELAY_GAME);

                isCal = true;

            }
        });

        btn_stop.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                isCal = false;
                sensorManager.unregisterListener(MainActivity.this);

                tv_stand.setBackgroundColor(Color.GREEN);
                tv_walk.setBackgroundColor(Color.LTGRAY);
                tv_run.setBackgroundColor(Color.LTGRAY);
                tv_cycle.setBackgroundColor(Color.LTGRAY);
                tv_incar.setBackgroundColor(Color.LTGRAY);
            }
        });

    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(MainActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)  {
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
                    calDelay++;

                    // every 50, calculate the measure once
                    if(calDelay % 50 == 0) {
                        float[] mean = mean(accBuffer);
                        float[] std = std(accBuffer, mean);
                        float[] max = max(accBuffer);
                        float[] min = min(accBuffer);

                        // new an instance
                        Instances measureInstance = new Instances("MeasureInstances", atts, 0);
                        measureInstance.setClassIndex(12);

                        // set measurement to instance values
                        double[] instanceValue = new double[13];

                        // set measure value to each attribution in the instance
                        setInstanceAttributionValues(instanceValue,mean,std,max,min);

                        measureInstance.add(new DenseInstance(1.0,instanceValue));

//                        String loadModelName = "knnmodel.model";
                        String loadModelName = "tree_j48.model";


                        try {
                            activityRegModel(loadModelName,measureInstance);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                    }

                }
                count++;

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



    private void setInstanceAttributions(ArrayList<Attribute> atts, ArrayList<String> classVal) {
        classVal.add("A");
        classVal.add("B");
        classVal.add("C");
        classVal.add("E");

        atts.add(new Attribute("F1"));
        atts.add(new Attribute("F2"));
        atts.add(new Attribute("F3"));
        atts.add(new Attribute("F4"));
        atts.add(new Attribute("F5"));
        atts.add(new Attribute("F6"));
        atts.add(new Attribute("F7"));
        atts.add(new Attribute("F8"));
        atts.add(new Attribute("F9"));
        atts.add(new Attribute("F10"));
        atts.add(new Attribute("F11"));
        atts.add(new Attribute("F12"));


        // CLASS  1
        atts.add(new Attribute("class", classVal));

    }

    private void setInstanceAttributionValues(double[] instanceValue,
                                              float[] mean, float[] std,
                                              float[] max, float[] min) {

        instanceValue[0] = mean[0];
        instanceValue[1] = std[0];
        instanceValue[2] = max[0];
        instanceValue[3] = min[0];
        instanceValue[4] = mean[1];
        instanceValue[5] = std[1];
        instanceValue[6] = max[1];
        instanceValue[7] = min[1];
        instanceValue[8] = mean[2];
        instanceValue[9] = std[2];
        instanceValue[10] = max[2];
        instanceValue[11] = min[2];

        // class 1
        instanceValue[12] = 0; // CLASS

    }

    //load model
    private void activityRegModel(String loadModelName, Instances measureInstance) throws Exception {

        InputStream is = this.getAssets().open(loadModelName);
        ObjectInputStream ois = new ObjectInputStream(is);
        Classifier cls = (Classifier) ois.readObject();
        ois.close();

        int pos = 0;
        if(cls == null) {
            return;
        }

        double value = cls.classifyInstance(measureInstance.instance(pos));

        String type = measureInstance.classAttribute().value((int)value);

        if(type.equals("A")) {
            tv_stand.setBackgroundColor(Color.GREEN);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("B")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.GREEN);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("C")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.GREEN);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("D")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.GREEN);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("E")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.GREEN);
        }

    }



    private float[] mean(ArrayList<float[]> input) {
        if(input == null || input.size() == 0 || input.get(0).length != 3)
            return null;

        float[] sum = new float[3];
        float[] results = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            for(int i=0; i < input.size(); i++){
                sum[CH] += input.get(i)[CH];
            }
            results[CH] = sum[CH] / input.size();
        }

        return results;

    }

    private float[] std(ArrayList<float[]> input, float[] mean) {
        if(input == null || input.size() == 0 || input.get(0).length != 3
                || mean == null || mean.length != 3) return null;

        float[] sum = new float[3];
        float[] results = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            for(int i=0; i < input.size(); i++){
                sum[CH] += (input.get(i)[CH] - mean[CH]) * (input.get(i)[CH] - mean[CH]);
            }
            results[CH] = (float) Math.sqrt(sum[CH] / input.size());
        }

        return results;

    }

    private float[] max(ArrayList<float[]> input) {
        if(input == null || input.size() == 0 || input.get(0).length != 3)
            return null;

        float[] max = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            max[CH] = Integer.MIN_VALUE;
            for(int i=0; i < input.size(); i++){
                if(input.get(i)[CH] > max[CH]) {
                    max[CH] = input.get(i)[CH];
                }

            }
        }

        return max;

    }

    private float[] min(ArrayList<float[]> input) {
        if(input == null || input.size() == 0 || input.get(0).length != 3)
            return null;

        float[] min = new float[3];

        for(int CH = 0; CH < 3; CH++) {
            min[CH] = Integer.MAX_VALUE;
            for(int i=0; i < input.size(); i++){
                if(input.get(i)[CH] < min[CH]) {
                    min[CH] = input.get(i)[CH];
                }

            }
        }

        return min;

    }


}
