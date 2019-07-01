package ir.markazandroid.police;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import ir.markazandroid.police.activity.authentication.LoginActivity;
import ir.markazandroid.police.bluetooth.BluetoothServer;
import ir.markazandroid.police.downloader.AppUpdater;
import ir.markazandroid.police.downloader.Downloader;
import ir.markazandroid.police.hardware.PortReader;
import ir.markazandroid.police.hardware.SensorMeter;
import ir.markazandroid.police.network.JSONParser.Parser;
import ir.markazandroid.police.network.NetworkClient;
import ir.markazandroid.police.network.socket.Message;
import ir.markazandroid.police.network.socket.WebSocketConfiguration;
import ir.markazandroid.police.network.socket.WebSocketManager;
import ir.markazandroid.police.object.ErrorObject;
import ir.markazandroid.police.object.FieldError;
import ir.markazandroid.police.object.Phone;
import ir.markazandroid.police.object.Status;
import ir.markazandroid.police.object.Version;
import ir.markazandroid.police.signal.Signal;
import ir.markazandroid.police.signal.SignalManager;
import ir.markazandroid.police.signal.SignalReceiver;
import ir.markazandroid.police.util.Console;
import ir.markazandroid.police.util.LocationMgr;
import ir.markazandroid.police.util.PackageManager;
import ir.markazandroid.police.util.PreferencesManager;
import ir.markazandroid.police.util.Utils;

/**
 * Coded by Ali on 2/27/2019.
 */
public class PoliceApplication extends Application implements SignalReceiver {

    private NetworkClient networkClient;
    private Phone phone;
    private SignalManager signalManager;
    private Parser parser;
    private PreferencesManager preferencesManager;
    private PortReader portReader;
    private Console console;
    private WebSocketManager socketManager;
    private boolean isInternetConnected = false;
    private SensorMeter sensorMeter;
    private LocationMgr locationMgr;
    private Downloader downloader;
    private AppUpdater appUpdater;
    private PackageManager packageManager;
    private BluetoothServer bluetoothServer;

    @Override
    public void onCreate() {
        super.onCreate();
        getSignalManager().addReceiver(this);
        Log.e("version",BuildConfig.VERSION_NAME);

        Intent intent = new Intent(this,PoliceService.class);
        startService(intent);

        getSocketManager().addMessageListener(this::onSocketMessage);

        //getPortReader().start();
        if (getPreferencesManager().getArduinoOnOffTime()!=null){
            String command = getPreferencesManager().getArduinoOnOffTime();
            setArdunoTime(command);
        }

        for (String cmd:getPreferencesManager().getStartupCommands()){
            Message message = new Message();
            message.setMessage(cmd);
            message.setMessageId("STARTUP");
            message.setType("STARTUP");
            message.setTime(System.currentTimeMillis());
            onSocketMessage(message);
        }

        getLocationMgr().start();
        //getBluetoothServer().start();
        //Log.e("oh","oh");


        checkTaxiBoardVersion();

        //getConsole().w("pm install -r -d /mnt/sdcard/police/pmcd/app4320.apk");
    }

    private void checkTaxiBoardVersion() {
        String packageName = "com.taxiboard.board";
        try {
            PackageInfo pi = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            int versionNumber = pi.versionCode;
            if (versionNumber < 142)
                getOwnPackageManager().installTaxiBoard();

        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean setArdunoTime(String command){
        //17:0:9:0
        String[] times = command.split(":");
        Calendar calendar = Calendar.getInstance();
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinute= calendar.get(Calendar.MINUTE);
        int now = Integer.parseInt(String.format(Locale.US, "%02d%02d", nowHour, nowMinute));
        int off = Integer.parseInt(times[0]+times[1]);
        int on = Integer.parseInt(times[2]+times[3]);

        if (on==off && on==0){
            return getPortReader().write(generateArduinoTime(command));
        }
        if (on==off) return false;

        if ((off<on && now >= off && now < on) || (off>on && !(now >= on && now < off))) {
            int rem = 60-nowMinute;
            if (rem<=5){
                nowHour+=1;
                nowMinute=5-rem;
            }
            else nowMinute+=5;

            if (Integer.parseInt(String.format(Locale.US, "%02d%02d", nowHour, nowMinute)) >= on && Integer.parseInt(String.format(Locale.US, "%02d%02d", nowHour, nowMinute)) - on < 6) {
                return getPortReader().write(generateArduinoTime(command));
            }
            command=nowHour+":"+nowMinute+":"+times[2]+":"+times[3];
            new Handler(getMainLooper()).post(()->Toast.makeText(this,"Off Time, turning off 5 minute",Toast.LENGTH_LONG).show());
            return getPortReader().write(generateArduinoTime(command));

        }
        return getPortReader().write(generateArduinoTime(command));
    }

    public void onSocketMessage(Message message){

        if (message.getMessage()==null) return;

        if (message.getMessage().startsWith("terminal ")){
            Message outputMessage = new Message();
            outputMessage.setMessageId(message.getMessageId());
            outputMessage.setType(Message.RESPONSE);
            String tcmd = message.getMessage().substring("terminal ".length());
            if (tcmd.startsWith("-w ")){
                tcmd = tcmd.substring("-w ".length());
                getConsole().w(tcmd
                        , (resultCode, output) -> {
                            outputMessage.setTime(System.currentTimeMillis());
                            outputMessage.setMessage("Process exit code="+resultCode+"\r\n"+output);
                            getSocketManager().send(getParser().get(outputMessage).toString());
                        });
            }
            else
                getConsole().executeAsync(tcmd
                        , (resultCode, output) -> {
                            outputMessage.setTime(System.currentTimeMillis());
                            outputMessage.setMessage("Process exit code="+resultCode+"\r\n"+output);
                            getSocketManager().send(getParser().get(outputMessage).toString());
                        });
        }
        else if (message.getMessage().startsWith("arduino ")){
            //T:HH:MM:SS:DD:MM:YY:HH:MM:HH:MM#
            String command = message.getMessage().substring("arduino ".length());
            if(command.startsWith("setTime ")){
                String onOffTime = command.substring("setTime ".length());
                //command = generateArduinoTime(onOffTime);
                getPreferencesManager().setArduinoOnOffTime(onOffTime);
                Message outputMessage = new Message();
                outputMessage.setMessageId(message.getMessageId());
                outputMessage.setType(Message.RESPONSE);
                outputMessage.setSuccess(setArdunoTime(onOffTime));
                outputMessage.setTime(System.currentTimeMillis());
                outputMessage.setMessage("non");
                getSocketManager().send(getParser().get(outputMessage).toString());
            }

        }
        else if (message.getMessage().startsWith("system ")){
            String command = message.getMessage().substring("system ".length());
            Message outputMessage = new Message();
            outputMessage.setMessageId(message.getMessageId());
            outputMessage.setType(Message.RESPONSE);
            outputMessage.setTime(System.currentTimeMillis());
            outputMessage.setMessage("non");
            //outputMessage.setSuccess(true);

            if (command.startsWith("get ")) {
                String ts = command.substring("get ".length());
                String getResult = getValue(ts);
                outputMessage.setMessage(getResult);
                outputMessage.setSuccess(true);
                getSocketManager().send(getParser().get(outputMessage).toString());

            } else if (command.startsWith("set ")) {
                String ts = command.substring("set ".length());
                getPreferencesManager().addStartupCommand(ts);
                outputMessage.setMessage("Done");
                outputMessage.setSuccess(true);
                getSocketManager().send(getParser().get(outputMessage).toString());
            }
            else if (command.startsWith("unset ")){
                String ts = command.substring("unset ".length());
                getPreferencesManager().removeStartupCommand(ts);
                outputMessage.setMessage("Done");
                outputMessage.setSuccess(true);
                getSocketManager().send(getParser().get(outputMessage).toString());
            }
            else if (command.startsWith("dinstall ")){
                String ts = command.substring("dinstall ".length());
                getOwnPackageManager().downloadAndInstall(ts, (processCode, status) -> {
                    if (processCode==PackageManager.PROGRESS_SUCCESS) {
                        outputMessage.setSuccess(true);
                        getSocketManager().send(getParser().get(outputMessage).toString());
                    }
                    else
                        broadcastMessage(status,message.getMessageId());

                },0);
                outputMessage.setMessage("Done");
            }
            else if (command.startsWith("dinstallt ")){
                String ts = command.substring("dinstallt ".length());
                getOwnPackageManager().downloadAndInstall(ts, (processCode, status) -> {
                    if (processCode==PackageManager.PROGRESS_SUCCESS) {
                        outputMessage.setSuccess(true);
                        getSocketManager().send(getParser().get(outputMessage).toString());
                    }
                    else
                        broadcastMessage(status,message.getMessageId());

                },1);
                outputMessage.setMessage("Done");
            }
            else{
                switch (command){
                    case "disconnect": getSocketManager().disconnect(); break;
                   // case "disable input": getPreferencesManager().addStartupCommand("system disable input"); disableInput(); break;
                   // case "enable input": getPreferencesManager().removeStartupCommand("system disable input"); enableInput(); break;
                    default:
                        outputMessage.setSuccess(false);
                        outputMessage.setMessage("Unknown System Command \""+command+"\"");
                }
                getSocketManager().send(getParser().get(outputMessage).toString());
            }
        }

        else{
            Message outputMessage = new Message();
            outputMessage.setMessageId(message.getMessageId());
            outputMessage.setType(Message.RESPONSE);
            outputMessage.setTime(System.currentTimeMillis());
            switch (message.getMessage()){
                case "hi":
                    outputMessage.setMessage("Hi"); break;

                case "ready":
                    outputMessage.setMessage("YESSIR"); break;

                default:
                    outputMessage.setMessage("Unknown Message \""+message.getMessage()+"\""); break;

            }
            //T:HH:MM:SS:DD:MM:YY:HH:MM:HH:MM#
            getSocketManager().send(getParser().get(outputMessage).toString());

        }
    }

    private void broadcastMessage(String msg,String messageId){
        Log.e("Message",msg);
        Message message = new Message();
        message.setTime(System.currentTimeMillis());
        message.setMessage(msg);
        message.setType(Message.MESSAGE);
        message.setMessageId(messageId==null?UUID.randomUUID().toString():messageId);
        getSocketManager().send(getParser().get(message).toString());
    }


    private String generateArduinoTime(String onOffTime){
        return Utils.getNowForArduino() + onOffTime+"#";
    }

    private String getValue(String getCommand) {
        switch (getCommand) {
            case "bmac":
                return BluetoothAdapter.getDefaultAdapter().getAddress();
            default:
                return "Unknown getCommand";
        }
    }



    public Console getConsole() {
        if (console==null) console=new Console();
        return console;
    }

    public PortReader getPortReader() {
        if (portReader==null) portReader=new PortReader(this);
        return portReader;
    }

    public WebSocketManager getSocketManager() {
        if (socketManager==null) {
            socketManager=new WebSocketManager(getNetworkClient(), getParser());
        }
        return socketManager;
    }


    public PreferencesManager getPreferencesManager() {
        if (preferencesManager==null) preferencesManager = new PreferencesManager(this);
        return preferencesManager;
    }

    public Parser getParser() {
        if (parser==null) {
            try {
                parser = new Parser();
                parser.addClass(Phone.class);
                parser.addClass(Message.class);
                parser.addClass(ErrorObject.class);
                parser.addClass(FieldError.class);
                parser.addClass(Status.class);
                parser.addSubClass(WebSocketConfiguration.class);
                parser.addClass(Version.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
        return parser;
    }


    public NetworkClient getNetworkClient(){
        if (networkClient==null){
            networkClient=new NetworkClient(getPreferencesManager());
        }
        return networkClient;
    }

    public SignalManager getSignalManager() {
        if (signalManager == null) signalManager = new SignalManager(this);
        return signalManager;
    }


    public boolean isInternetConnected() {
        return isInternetConnected;
    }

    public SensorMeter getSensorMeter() {
        if (sensorMeter==null) sensorMeter=new SensorMeter(this);
        return sensorMeter;
    }

    public LocationMgr getLocationMgr() {
        if(locationMgr==null) locationMgr=new LocationMgr(this);
        return locationMgr;
    }

    @Override
    public boolean onSignal(Signal signal) {
        if (signal.getType() == Signal.SIGNAL_LOGIN) {
            setPhone((Phone) signal.getExtras());
            Log.e(PoliceApplication.this.toString(), "login signal received " /*+ user.getUsername()*/);
            return true;
        } else if (signal.getType() == Signal.SIGNAL_LOGOUT) {
            Log.e(PoliceApplication.this.toString(), "logout signal received ");
            getNetworkClient().deleteCookies();
            //DeleteToken deleteToken = new DeleteToken();
            //deleteToken.execute();
            setPhone(null);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        else if (signal.getType()==Signal.START_MAIN_ACTIVITY){
            /*if (getFrontActivity()==null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }*/
        }
        else if (signal.getType()==Signal.OPEN_SOCKET_HEADER_RECEIVED){
            getSocketManager().connect();
        }else if (signal.getType()==Signal.DOWNLOADER_NO_NETWORK){
            isInternetConnected=false;
        }else if (signal.getType()==Signal.DOWNLOADER_NETWORK){
            isInternetConnected=true;
        }
        return false;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        if (phone!=null)
            getPreferencesManager().setPhone(getParser().get(phone).toString());
        this.phone = phone;
    }

    public Downloader getDownloader() {
        if (downloader==null) downloader=new Downloader(this,getNetworkClient());
        return downloader;
    }

    public AppUpdater getAppUpdater() {
        if (appUpdater==null) appUpdater=new AppUpdater(getDownloader(),getConsole());
        return appUpdater;
    }

    public PackageManager getOwnPackageManager() {
        if (packageManager==null) packageManager=new PackageManager(getDownloader(),this,getConsole());
        return packageManager;
    }

    public BluetoothServer getBluetoothServer() {
        if (bluetoothServer == null) {
            bluetoothServer = new BluetoothServer(this);
            //bluetoothServer.start();
        }
        return bluetoothServer;
    }
}
