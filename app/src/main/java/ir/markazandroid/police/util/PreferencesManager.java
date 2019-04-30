package ir.markazandroid.police.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Coded by Ali on 09/01/2018.
 */

public class PreferencesManager {


    private static final String ARDUINO_TIME="ir.markazandroid.advertiser.util.PreferencesManager.ARDUINO_TIME";
    private static final String STARTUP_CMDS="ir.markazandroid.advertiser.util.PreferencesManager.STARTUP_CMDS";
    private static final String PHONE="ir.markazandroid.advertiser.util.PreferencesManager.PHONE";

    private SharedPreferences privateSharedPreferences;

    public PreferencesManager(Context context){
        this.privateSharedPreferences =context.getSharedPreferences("ir.markazandroid.advertiser.util.privateSharedPreferences",Context.MODE_PRIVATE);
    }

    public String getArduinoOnOffTime(){
        return privateSharedPreferences.getString(ARDUINO_TIME,null);
    }
    public void setArduinoOnOffTime(String onOffTime){
        privateSharedPreferences.edit().putString(ARDUINO_TIME,onOffTime).apply();
    }

    public Set<String> getStartupCommands(){
        return privateSharedPreferences.getStringSet(STARTUP_CMDS,new HashSet<>());
    }
    public synchronized void addStartupCommand(String command){
        Set<String> cmds = getStartupCommands();
        cmds.add(command);
        privateSharedPreferences.edit().putStringSet(STARTUP_CMDS,cmds).apply();
    }

    public synchronized void removeStartupCommand(String command){
        Set<String> cmds = new HashSet<>(getStartupCommands());
        cmds.remove(command);
        privateSharedPreferences.edit().putStringSet(STARTUP_CMDS,cmds).apply();
    }

    public SharedPreferences getPrivateSharedPreferences() {
        return privateSharedPreferences;
    }

    public String getPhone(){
        return privateSharedPreferences.getString(PHONE,null);
    }
    public void setPhone(String phoneString){
        privateSharedPreferences.edit().putString(PHONE,phoneString).apply();
    }

}
