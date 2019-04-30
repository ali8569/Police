package ir.markazandroid.police.object;

import java.io.Serializable;

import ir.markazandroid.police.network.JSONParser.annotations.JSON;

/**
 * Coded by Ali on 5/24/2018.
 */
public class Version implements Serializable {
    private int version;
    private String url;
    private String name;
    private String model;
    private boolean allowDowngrade;

    @JSON
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @JSON
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JSON
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @JSON
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JSON
    public boolean getAllowDowngrade() {
        return allowDowngrade;
    }

    public void setAllowDowngrade(boolean allowDowngrade) {
        this.allowDowngrade = allowDowngrade;
    }
}
