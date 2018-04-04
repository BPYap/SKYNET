package com.skynet.hotspotdatabase;

import android.arch.persistence.room.Entity;

/**
 * Created by eddyl on 24/3/2018.
 */

@Entity(primaryKeys = {"index"})
public class Hotspot {
    private int index;

    private double lattitude;

    private double longtitude;

    private int addressPostalCode;

    private String description;

    private String name;

    private String addressStreetName;

    private String operatorName;

    public Hotspot(int index, double lattitude, double longtitude, int addressPostalCode, String description,
                   String name, String addressStreetName, String operatorName){
        this.index = index;
        this.lattitude = lattitude;
        this.longtitude = longtitude;
        this.addressPostalCode = addressPostalCode;
        this.description = description;
        this.name = name;
        this.addressStreetName = addressStreetName;
        this.operatorName = operatorName;
    }

    public int getIndex() {
        return index;
    }

    public double getLattitude(){
        return lattitude;
    }

    public double getLongtitude(){
        return longtitude;
    }

    public int getAddressPostalCode(){
        return addressPostalCode;
    }

    public String getDescription(){
        return description;
    }

    public String getName(){
        return name;
    }

    public String getAddressStreetName(){
        return  addressStreetName;
    }

    public String getOperatorName(){
        return operatorName;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLattitude(double lattitude){
        this.lattitude = lattitude;
    }

    public void setLongtitude(double longtitude){
        this.longtitude = longtitude;
    }

    public void setAddressPostalCode(int addressPostalCode){
        this.addressPostalCode = addressPostalCode;
    }

    public void setAddressStreetName(String addressStreetName) {
        this.addressStreetName = addressStreetName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
    
}
