package ir.markazandroid.police.activity;

import android.support.v7.app.AppCompatActivity;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.network.NetworkManager;
import ir.markazandroid.police.network.NetworkMangerImp;
import ir.markazandroid.police.signal.Signal;
import ir.markazandroid.police.signal.SignalManager;
import ir.markazandroid.police.util.PreferencesManager;

/**
 * Coded by Ali on 02/02/2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
       // ((PoliceApplication)getApplicationContext()).setFrontActivity(getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //((PoliceApplication)getApplicationContext()).setFrontActivity(null);
    }

    PreferencesManager getPreferencesManager(){
        return  ((PoliceApplication) getApplication()).getPreferencesManager();
    }

    private NetworkManager networkManager;


    protected NetworkManager getNetworkManager() {
        if (networkManager==null){
            networkManager= new NetworkMangerImp.NetworkManagerBuilder()
                    .from(this)
                    .tag(toString())
                    .build();
        }
        return networkManager;
    }

    protected SignalManager getSignalManager(){
        return ((PoliceApplication) getApplication()).getSignalManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSignalManager().sendSignal(new Signal(toString(), Signal.SIGNAL_ACTIVITY_DESTROYED));
    }
}
