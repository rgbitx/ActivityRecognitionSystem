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
import weka.core.Instances;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button btn_start, btn_stop;
    TextView tv_stand, tv_walk, tv_run, tv_cycle, tv_incar;
    private SensorManager sensorManager;
    Sensor accSensor, gyroSensor, magSensor;

    MeasureCalculation measureCal;
    FFT fft;
    int fs = 50;

    ArrayList<float[]> accBuffer;
    float acc_rms = 0, acc_mean = 0, acc_std = 0, acc_max = 0, acc_min = 0, acc_psd_rms=0;
    ArrayList<float[]> gyroBuffer;
    float gyro_rms = 0, gyro_mean = 0, gyro_std = 0, gyro_max = 0, gyro_min = 0;
    int bufferSize = 16;
    int calDelay;
    boolean isCal;
    int count_acc;
    int count_gyro;
    boolean isAccCal = false;
    boolean isGyroCal = false;

    float[] input;

    float[] acc;
    float[] gyro;
    float[] mag;

    String ModelName = "tree_j48.model";

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

        measureCal = new MeasureCalculation();
        fft = new FFT(bufferSize);

        accBuffer = new ArrayList<>();
        gyroBuffer = new ArrayList<>();
        count_acc = 0;
        count_gyro = 0;

        calDelay = 0;
        isCal = false;

        // Model initialization
        atts = new ArrayList<>();
        classVal = new ArrayList<>();

        // set the attributions of the instance
        setInstanceAttributions(atts, classVal);

//        //test FFT
//        // Single sin
//        int N = 8;
//        double[] re = new double[N];
//        double[] im = new double[N];
//
//        for(int i=0; i<N; i++) {
//           re[i] = Math.cos(2*Math.PI*i / N);
//           im[i] = 0;
//         }
//
//        FFT fft1 = new FFT(N);
//        beforeAfter(fft1, re, im);


        //get sensor manage
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        btn_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                sensorManager.registerListener(MainActivity.this, accSensor, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(MainActivity.this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(MainActivity.this, magSensor, SensorManager.SENSOR_DELAY_GAME);

                isCal = true;

            }
        });


        btn_stop.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                isCal = false;
                sensorManager.unregisterListener(MainActivity.this);
                sensorManager.unregisterListener(MainActivity.this);

                tv_stand.setBackgroundColor(Color.LTGRAY);
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
        sensorManager.registerListener(MainActivity.this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
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
                if(count_acc < bufferSize) {
                    accBuffer.add(acc);
                } else {
                    accBuffer.remove(0);
                    accBuffer.add(acc);
                }
                count_acc++;
            }

        }

        if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            gyro = event.values;

            // control the calculation
            if(isCal) {
                //put the data into the buffer
                if(count_gyro < bufferSize) {
                    gyroBuffer.add(gyro);
                } else {
                    gyroBuffer.remove(0);
                    gyroBuffer.add(gyro);
                }
                count_gyro++;
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
            mag = event.values;
        }

        if(count_acc > bufferSize && (count_acc-bufferSize) % 150 == 0) {
            float[] acc = measureCal.ovalMod(accBuffer);
            acc_rms = measureCal.rms(acc);
            acc_mean = measureCal.mean(acc);
            acc_std = measureCal.std(acc, acc_mean);
            acc_max = measureCal.max(acc);
            acc_min = measureCal.min(acc);

            // calculate psd and mean frequency
            double[] re = new double[bufferSize];
            double[] im = new double[bufferSize];

            for(int i=0; i<bufferSize; i++) {
                re[i] = acc[i]-acc_mean;
                im[i] = 0;
            }
            fft.fft(re,im);
            acc_psd_rms = (float) fft.psd_rms(re,im,fs);


            isAccCal = true;
        }

        // every 50, calculate the measure once
        if(count_gyro > bufferSize && (count_gyro-bufferSize) % 150 == 0) {

            float[] gyro = measureCal.ovalMod(gyroBuffer);
            gyro_rms = measureCal.mean(gyro);
            gyro_mean = measureCal.mean(gyro);
            gyro_std = measureCal.std(gyro, gyro_mean);
            gyro_max = measureCal.max(gyro);
            gyro_min = measureCal.min(gyro);

            isGyroCal = true;
        }

        if(isAccCal && isGyroCal) {

            int totalVariablesNum = 5;

            // new an instance
            Instances measureInstance = new Instances("MeasureInstances", atts, 0);
            measureInstance.setClassIndex(totalVariablesNum-1);

            // set measurement to instance values
            double[] instanceValue = new double[totalVariablesNum];

            input = new float[] {acc_rms, acc_std, gyro_rms, gyro_std};

            // set measure value to each attribution in the instance
            setInstanceAttributionValues(instanceValue, input);

            measureInstance.add(new DenseInstance(1.0, instanceValue));

            String loadModelName = ModelName;

            try {
                activityRegModel(loadModelName, measureInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }

            isAccCal = false;
            isGyroCal = false;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void setInstanceAttributions(ArrayList<Attribute> atts, ArrayList<String> classVal) {
        classVal.add("STILL");
        classVal.add("WALK");
        classVal.add("RUN");
        classVal.add("BIKE");
        classVal.add("CAR");

        atts.add(new Attribute("F1"));
        atts.add(new Attribute("F2"));
        atts.add(new Attribute("F3"));
        atts.add(new Attribute("F4"));


        // CLASS  1
        atts.add(new Attribute("class", classVal));
    }


    private void
    setInstanceAttributionValues(double[] instanceValue,
                                              float[] inputData) {

        instanceValue[0] = inputData[0];
        instanceValue[1] = inputData[1];
        instanceValue[2] = inputData[2];
        instanceValue[3] = inputData[3];

        // class 1
        instanceValue[4] = 0; // CLASS
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

        if(type.equals("STILL")) {
            tv_stand.setBackgroundColor(Color.GREEN);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("WALK")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.GREEN);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("RUN")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.GREEN);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("BIKE")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.GREEN);
            tv_incar.setBackgroundColor(Color.LTGRAY);
        } else if(type.equals("CAR")) {
            tv_stand.setBackgroundColor(Color.LTGRAY);
            tv_walk.setBackgroundColor(Color.LTGRAY);
            tv_run.setBackgroundColor(Color.LTGRAY);
            tv_cycle.setBackgroundColor(Color.LTGRAY);
            tv_incar.setBackgroundColor(Color.GREEN);
        }

    }


       protected void beforeAfter(FFT fft, double[] re, double[] im) {
             System.out.println("Before: ");
             printReIm(re, im);
             fft.fft(re, im);
             double[] power = fft.psd(re,im,fs);
             System.out.println("After: ");
             printReIm(re, im);
           }

       protected void printReIm(double[] re, double[] im) {
             System.out.print("Re: [");
             for(int i=0; i<re.length; i++)
                   System.out.print(((int)(re[i]*1000)/1000.0) + " ");

             System.out.print("]\nIm: [");
             for(int i=0; i<im.length; i++)
                   System.out.print(((int)(im[i]*1000)/1000.0) + " ");

             System.out.println("]");
       }



}
