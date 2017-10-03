package com.facefengshui.two;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "facefengshuiresults")
public class FaceFengshuiResultItem {
    private String result_id;
    private double eye_distance_ratio;
    private double mouth_size_ratio;
    private double philtrum_length_ratio;

    @DynamoDBHashKey(attributeName = "result_id")
    public String getResultId() {
        return result_id;
    }

    public void setResultId(String result_id) {
        this.result_id = result_id;
    }

    @DynamoDBAttribute(attributeName = "eye_distance_ratio")
    public double getEyeDistanceRatio() {
        return eye_distance_ratio;
    }

    public void setEyeDistanceRatio(double eye_distance_ratio) {
        this.eye_distance_ratio = eye_distance_ratio;
    }

    @DynamoDBAttribute(attributeName = "mouth_size_ratio")
    public double getMouthSizeRatio() {
        return mouth_size_ratio;
    }

    public void setMouthSizeRatio(double mouth_size_ratio) {
        this.mouth_size_ratio = mouth_size_ratio;
    }

    @DynamoDBAttribute(attributeName = "philtrum_length_ratio")
    public double getPhiltrumLengthRatio() {
        return philtrum_length_ratio;
    }

    public void setPhiltrumLengthRatio(double philtrum_length_ratio) {
        this.philtrum_length_ratio = philtrum_length_ratio;
    }
}
