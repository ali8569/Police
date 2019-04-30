package ir.markazandroid.police;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.activity.PoliceActivity;
import ir.markazandroid.police.downloader.AppUpdater;
import ir.markazandroid.police.event.BaseEvent;
import ir.markazandroid.police.network.JSONParser.Parser;
import ir.markazandroid.police.network.NetworkClient;
import ir.markazandroid.police.network.NetworkManager;
import ir.markazandroid.police.network.NetworkMangerImp;
import ir.markazandroid.police.network.OnResultLoaded;
import ir.markazandroid.police.network.socket.Message;
import ir.markazandroid.police.network.socket.WebSocketManager;
import ir.markazandroid.police.object.Status;
import ir.markazandroid.police.signal.Signal;
import ir.markazandroid.police.signal.SignalManager;

public class PoliceService extends Service {

    private Timer timer;
    private boolean isStarted=false;
    private static final int ONGOING_NOTIFICATION_ID=123456;
    private SignalManager signalManager;
    private NetworkManager networkManager;
    private IBinder mBinder;
    private Handler handler;
    private long lastTimeStatus=0;
    private WebSocketManager webSocketManager;

    public PoliceService() {
       mBinder = new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        signalManager=((PoliceApplication)getApplication()).getSignalManager();
        networkManager= new NetworkMangerImp.NetworkManagerBuilder()
                .from(this)
                .tag(toString())
                .build();
        handler=new Handler(getMainLooper());

        webSocketManager=((PoliceApplication)getApplication()).getSocketManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doWork();
        return Service.START_STICKY;
    }

    private void doWork() {
        if (isStarted) return;

        Log.e("Service","started");


        timer=new Timer();
        //setWorkingNotification();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getStatus();
               // signalManager.sendMainSignal(new Signal(Signal.START_MAIN_ACTIVITY));
            }
        },0,25_000);

        isStarted=true;
    }

    private void getStatus() {
        networkManager.getStatus(lastTimeStatus,new OnResultLoaded<Status>() {
            @Override
            public void loaded(Status result) {
                runOnUiThread(() -> onStatus(result));
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onStatus(Status status){
        signalManager.sendMainSignal(new Signal(Signal.SIGNAL_DEVICE_AUTHENTICATED));

        if (lastTimeStatus==0){
            sendBroadcast(BaseEvent.getDeviceAuthenticatedIntent());
        }
        if (status.getLastTime()!=0) lastTimeStatus=status.getLastTime();

        if (status.getVersion()!=null){
            getAppUpdater().update(status.getVersion(), new AppUpdater.UpdateProgressListener() {
                @Override
                public void onStart() {
                    broadcastMessage("Started To update");
                }

                @Override
                public void onProgress(String progress) {
                    broadcastMessage(progress);
                }
            });
        }
    }

    private void broadcastMessage(String msg){
        Log.e("Message",msg);
        Message message = new Message();
        message.setTime(System.currentTimeMillis());
        message.setMessage(msg);
        message.setType(Message.MESSAGE);
        message.setMessageId(UUID.randomUUID().toString());
        webSocketManager.send(getParser().get(message).toString());
    }



    private void runOnUiThread(Runnable runnable){
        handler.post(runnable);
    }

    private void setWorkingNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Police is Working...")
                .setTicker("Started Police Service")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);


        Intent manager = new Intent(this, PoliceActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, manager, 0);
        notification.setContentIntent(resultPendingIntent);

        startForeground(ONGOING_NOTIFICATION_ID, notification.build());
    }

    public void stopAndRelease(){
        Log.e("Service","stoped");
        if (timer!=null)
            timer.cancel();

        /*NotificationCompat.Builder notification = new NotificationCompat.Builder(this,getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Police is Stopped...")
                .setTicker("Police is Stopped")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent manager = new Intent(this, PoliceActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, manager, 0);
        notification.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification.build());*/
        isStarted=false;
    }

    @Override
    public boolean stopService(Intent name) {
        stopAndRelease();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAndRelease();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public PoliceService getService() {
            return PoliceService.this;
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    private AppUpdater getAppUpdater(){
        return ((PoliceApplication)getApplication()).getAppUpdater();
    }
    private Parser getParser(){
        return ((PoliceApplication)getApplication()).getParser();
    }

}
