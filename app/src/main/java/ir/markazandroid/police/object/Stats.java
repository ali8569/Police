package ir.markazandroid.police.object;

import java.io.Serializable;

import ir.markazandroid.police.network.formdata.Form;

/**
 * Coded by Ali on 3/3/2019.
 */
public class Stats implements Serializable {

    @Form
    private String versionName;
    @Form
    private int versionCode;

    @Form
    private String model = "IOT20A";

    @Form
    private String arduinoStats="NA";
    @Form
    private double lat;
    @Form
    private double lon;
    @Form
    private long timestamp;


    @Form
    private long lastTime;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getArduinoStats() {
        return arduinoStats;
    }

    public void setArduinoStats(String arduinoStats) {
        this.arduinoStats = arduinoStats;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return arduinoStats+"__v"
                + getVersionCode()
                +"__n"+getVersionName()+"#"
                +"__"
               // + getSensorMeter().getResults()+"__"
                + "lat="+getLat()+"lon="+getLon();
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
