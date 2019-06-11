package ir.markazandroid.police.object;

import java.io.Serializable;

import ir.markazandroid.police.network.JSONParser.annotations.JSON;
import ir.markazandroid.police.network.formdata.Form;

/**
 * Coded by Ali on 2/27/2019.
 */
public class LoginCredentials implements Serializable {

    @Form
    private String username;
    @Form
    private String password;

    @Form
    private String deviceId;

    @Form
    private String bluetoothMac;

    @JSON
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JSON
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JSON
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBluetoothMac() {
        return bluetoothMac;
    }

    public void setBluetoothMac(String bluetoothMac) {
        this.bluetoothMac = bluetoothMac;
    }
}
