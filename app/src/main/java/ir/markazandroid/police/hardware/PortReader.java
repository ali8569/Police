package ir.markazandroid.police.hardware;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.hardware.serial.SerialPort;
import ir.markazandroid.police.signal.Signal;
import ir.markazandroid.police.signal.SignalManager;
import ir.markazandroid.police.util.Roozh;

/**
 * Coded by Ali on 01/03/2018.
 */

public class PortReader extends Thread {

    private Context context;
    private Handler handler;
    private boolean isBlocked=false;
    private InputParser inputParser;
    private SerialPort serialPort;
    boolean isCancelled =false;
    public static String lastData="NA";
    private InputStream inputStream;
    private Timer timer;
    private int blockAcc,unBlockAcc;

    public PortReader(Context context) {
        this.context = context;
        blockAcc =0;
        unBlockAcc=0;


        /*for (SerialPort port:ports){
            Log.e("port", "PortReader: "+port.getDescriptivePortName()+" : "+port.getSystemPortName() );
        }*/



        handler=new Handler(context.getMainLooper());
        inputParser=new InputParser('\n', cmd -> {
            try {
                Log.e("command",cmd);
            /*if(cmd.equals("OFF")){
                isBlocked=true;
                sendBlockViewSignal();
            }*/
                cmd=cmd.replace('$','&')
                        .replaceAll("&","")
                        .replaceAll("#","")
                        .replaceAll("\r","")
                        .replaceAll("\n","");
                lastData=cmd;

                String[] dataArray = cmd.split(";");
                Map<String,String> dataMap = new HashMap<>();
                for(String data:dataArray){
                    String[] d = data.split(":");
                    dataMap.put(d[0],d[1]);
                }
                int d = Integer.parseInt(dataMap.get("d"));

                if (d < 100 && d > 0) {
                    if (!isBlocked) {
                        blockAcc++;
                        if (blockAcc >= 3) {
                            blockAcc = 0;
                            isBlocked = true;
                            sendBlockViewSignal();
                        }
                    } else {
                        unBlockAcc = 0;
                    }
                } else {
                    if (isBlocked) {
                        unBlockAcc++;
                        if (unBlockAcc >= 0) {
                            unBlockAcc = 0;
                            isBlocked = false;
                            sendUnBlockViewSignal();
                        }
                    } else {
                        blockAcc = 0;
                    }
                }
                //handler.post(()-> Toast.makeText(context,dataMap.get("d"),Toast.LENGTH_SHORT).show());
                Log.e("distance", dataMap.get("d") + "  ");
            }catch (Exception ignored){

            }

        });
        inputParser.init();


        /*PortInfo[] list = Serial.listPorts();

        for(PortInfo info:list){
            Log.e("port", "PortReader: "+info.description+" : "+info.hardwareId+" : "+info.port);
        }*/


        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                save(lastData);
            }
        },10_000,10_000);

        init();
    }


    public void init(){

        String portName = "/dev/ttyS2";

        try {
            serialPort = new SerialPort(new File(portName), 9600, 0);
            inputStream=serialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            if (serialPort != null)
                serialPort.close();
        }

    }


    @Override
    public void run() {

        byte[] buffer = new byte[1024];

        //isCancelled
        while (!isCancelled) {
            try {
                String read = readChar(inputStream,buffer);
                //Log.e("read",read);
                inputParser.addInput(read);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                init();
            }


        }
    }

    public String readChar(InputStream inputStream,byte[] buffer) throws IOException {
        int numRead = inputStream.read(buffer);
        while (numRead<1){
            try {
                if (isCancelled) break;
                Thread.sleep(1);
                numRead = inputStream.read(buffer);
            } catch (Exception e) {
                handler.post(()-> Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show());
                e.printStackTrace();
                break;
            }
        }
        return new String(buffer,0,numRead);
    }

    /*private void showToast(final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
            }
        });
    }
*/
    private SignalManager getSignalManager(){
        return ((PoliceApplication)context.getApplicationContext()).getSignalManager();
    }

    private void sendBlockViewSignal(){
        //handler.post(()-> Toast.makeText(context,"Block",Toast.LENGTH_SHORT).show());
        Signal signal = new Signal("screen block",Signal.SIGNAL_SCREEN_BLOCK);
        getSignalManager().sendMainSignal(signal);
    }

    private void sendUnBlockViewSignal(){
        //handler.post(()-> Toast.makeText(context,"Unblock",Toast.LENGTH_SHORT).show());
        Signal signal = new Signal("screen unblock",Signal.SIGNAL_SCREEN_UNBLOCK);
        getSignalManager().sendMainSignal(signal);
    }

    public void close(){
        try {
            isCancelled=true;
            if (timer!=null)
                timer.cancel();
            if(serialPort!=null) {
                serialPort.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean write(String toWrite){
        try {
            if (serialPort!=null) {
                serialPort.getOutputStream().write(toWrite.getBytes());
                handler.post(() -> Toast.makeText(context, "Wrote " + toWrite, Toast.LENGTH_SHORT).show());
                Log.e("wrote:", toWrite);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void save(String finalBitmap) {

        //getFilesDir()
        //openFileInput()
        //openFileOutput()
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/police/logs");
        myDir.mkdirs();
        String fname = Roozh.getCurrentTimeNo()+".txt";
        File file = new File (myDir, fname);
        try {
            FileWriter out =new FileWriter(file,true);
            out.append(Roozh.getTime(System.currentTimeMillis()))
                    .append("  --  ")
                    .append(finalBitmap)
                    .append("\r\n");
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
