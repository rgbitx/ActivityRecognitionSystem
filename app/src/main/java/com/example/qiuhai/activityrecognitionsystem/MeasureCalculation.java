package com.example.qiuhai.activityrecognitionsystem;

import java.util.ArrayList;

/**
 * Created by qiuhai on 2016/11/3.
 */

public class MeasureCalculation {


    public float[] ovalMod(ArrayList<float[]> input) {
        if(input == null || input.size() == 0 || input.get(0).length != 3)
            return null;

        float[] rets = new float[input.size()];
        float temp = 0;

        for(int i=0; i<input.size(); i++) {
            temp = (float) Math.sqrt(input.get(i)[0]*input.get(i)[0] +
                    input.get(i)[1]*input.get(i)[1] +
                    input.get(i)[2]*input.get(i)[2]);
            rets[i] = temp;
        }


        return rets;
    }


    public float rms(float[] input) {
        if(input == null || input.length == 0)
            return 0;

        float sum = 0;
        float results = 0;

        for(int i=0; i < input.length; i++){
            sum += input[i]*input[i];
        }
        results = (float) Math.sqrt(sum / input.length);

        return results;

    }

    public double rms(double[] input) {
        if(input == null || input.length == 0)
            return 0;

        float sum = 0;
        float results = 0;

        for(int i=0; i < input.length; i++){
            sum += input[i]*input[i];
        }
        results = (float) Math.sqrt(sum / input.length);

        return results;

    }



    public float mean(float[] input) {
        if(input == null || input.length == 0)
            return 0;

        float sum = 0;
        float results = 0;

        for(int i=0; i < input.length; i++){
            sum += input[i];
        }
        results = sum / input.length;

        return results;

    }


    public float[] mean(ArrayList<float[]> input) {
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

    public float std(float[] input, float mean) {
        if(input == null || input.length == 0)
            return 0;

        float sum = 0;
        float results = 0;

        for(int i=0; i < input.length; i++){
            sum += (input[i] - mean) * (input[i] - mean);
        }

        results = (float) Math.sqrt(sum / input.length);

        return results;

    }



    public float[] std(ArrayList<float[]> input, float[] mean) {
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



    public float max(float[] input) {
        if(input == null || input.length == 0)
            return 0;

        float max = Integer.MIN_VALUE;

        for(int i=0; i < input.length; i++){
            if(input[i] > max) {
                max = input[i];
            }

        }

        return max;

    }


    public float[] max(ArrayList<float[]> input) {
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


    public float min(float[] input) {
        if(input == null || input.length == 0)
            return 0;

        float min = Integer.MAX_VALUE;

        for(int i=0; i < input.length; i++){
            if(input[i] < min) {
                min = input[i];
            }
        }

        return min;

    }


    public float[] min(ArrayList<float[]> input) {
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
