package ir.markazandroid.police.bridge;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.aidl.AuthenticationDetails;
import ir.markazandroid.police.aidl.IPolice;
import ir.markazandroid.police.network.NetworkClient;
import ir.markazandroid.police.signal.Signal;
import ir.markazandroid.police.signal.SignalManager;
import ir.markazandroid.police.signal.SignalReceiver;
import okhttp3.Cookie;

public class BridgeService extends Service {

    private volatile AuthenticationDetails authenticationDetails;

    private SignalReceiver signalReceiver = signal -> {
        if (signal.getType()==Signal.SIGNAL_DEVICE_AUTHENTICATED){
            String sessionId = getSessionId();
            if (sessionId==null){
                authenticationDetails=null;
                return true;
            }
            authenticationDetails=new AuthenticationDetails();
            authenticationDetails.setLastUpdate(System.currentTimeMillis());
            authenticationDetails.setSessionId(sessionId);
            return true;
        }
        return false;
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getSignalManager().addReceiver(signalReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getSignalManager().removeReceiver(signalReceiver);
    }

    public BridgeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IPolice.Stub binder = new IPolice.Stub() {

        @Override
        public AuthenticationDetails getAuthenticationDetails() {
            if (authenticationDetails==null
                    || System.currentTimeMillis()-authenticationDetails.getLastUpdate()>=AuthenticationDetails.MAX_UPDATE_TIMEOUT)
                return null;

            return authenticationDetails;
        }
    };

    private SignalManager getSignalManager(){
        return ((PoliceApplication)getApplication()).getSignalManager();
    }

    private NetworkClient getNetworkClient(){
        return ((PoliceApplication)getApplication()).getNetworkClient();
    }

    private String getSessionId(){

        for(Cookie cookie:getNetworkClient().getCookie()){
            if (cookie.name().equals("JSESSIONID")|| cookie.name().equals("SESSION"))
                return cookie.value();
        }
        return null;
    }

}
