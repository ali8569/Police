package ir.markazandroid.police.object;

import java.io.Serializable;

import ir.markazandroid.police.network.JSONParser.annotations.JSON;

/**
 * Coded by Ali on 3/2/2019.
 */
public class Status implements Serializable {

    private long lastTime;
    private Version version;

    @JSON
    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    @JSON(classType = JSON.CLASS_TYPE_OBJECT,clazz = Version.class)
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
