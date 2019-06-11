package ir.markazandroid.police.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Coded by Ali on 1/20/2019.
 */
public class Console implements Closeable {

    private BufferedWriter writer;

    private Process process;

    public interface ConsoleOut{
        void onCommandFinished(int resultCode, String output);
    }


    public Console(){
       /* try {
            //change su to sh -- ali gholami
            process = Runtime.getRuntime().exec("su -e log -v console ali is good");
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    private void init(){
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    //General
    public void executeAs(String cmd, ConsoleOut consoleOut) {
        executeAsync(cmd, consoleOut);
    }

    public void execute(String cmd) {
        write(cmd);
    }



    public void executeAsync(String cmd,ConsoleOut consoleOut){
        new Thread(){
            @Override
            public void run() {
                try {
                    process=Runtime.getRuntime().exec("su");
                    ReaderThread readerThread = new ReaderThread(process);
                    readerThread.setConsoleOut(consoleOut);
                    readerThread.start();
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes(cmd+"\n");
                    os.flush();
                    os.writeBytes("exit\n");
                    os.flush();
                    process.waitFor();
                    //writer.write(s);
                    //writer.flush();
                    //process.waitFor();
                } catch (Exception e) {
                    consoleOut.onCommandFinished(-69,"Exception: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }.run();

    }

    public void write(String s){
        try {
            process=Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(s+"\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeWithoutExit(String s){
        try {
            process=Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(s+"\n");
            os.flush();
            //os.writeBytes("exit\n");
            //os.flush();
            //process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void w(String s,ConsoleOut consoleOut){
        try {
            process=Runtime.getRuntime().exec(s);
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.setConsoleOut(consoleOut);
            readerThread.start();
            //os.writeBytes("exit\n");
            //os.flush();
            process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void w(String s){
        try {
            process=Runtime.getRuntime().exec(s);
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            //os.writeBytes("exit\n");
            //os.flush();
            //process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wWait(String s){
        try {
            process=Runtime.getRuntime().exec(s);
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            //os.writeBytes("exit\n");
            //os.flush();
            process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public synchronized void updatePolice(String apkPath){
        try {

            process=Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            Log.e("Up","got here2");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(//"stop;"+
                    "mount -o rw,remount /system;"+
                    "mv "+apkPath+" /system/priv-app/Police/Police.apk;"+
                    "chmod 755 /system/priv-app/Police;"+
                    "chmod -R 644 /system/priv-app/Police;"+
                    "chmod 755 /system/priv-app/Police;" +
                            "chown root:root /system/priv-app/Police;"+
                            "chown root:root /system/priv-app/Police/Police.apk;"+
            "reboot\n");
            os.flush();
            Log.e("Up","got here3");
            /*process.waitFor();

            //process.waitFor();
            //process=Runtime.getRuntime().exec("su -c mount -o rw,remount /system");
            //process.waitFor();
            //Log.e("Up","got here1");
            //process=Runtime.getRuntime().exec("su -c stop");
            //new ReaderThread(process).start();
            //process.waitFor();
            Log.e("Up","got here2");
            process=Runtime.getRuntime().exec("mv "+apkPath+" /system/priv-app/Police/Police.apk");
            new ReaderThread(process).start();
            process.waitFor();
            Log.e("Up","got here3");
            process=Runtime.getRuntime().exec("chmod 755 /system/priv-app/Police");
            new ReaderThread(process).start();
            process.waitFor();
            Log.e("Up","got here4");
            process=Runtime.getRuntime().exec("chmod -R 644 /system/priv-app/Police");
            new ReaderThread(process).start();
            process.waitFor();
            Log.e("Up","got here5");
            process=Runtime.getRuntime().exec("chmod 755 /system/priv-app/Police");
            new ReaderThread(process).start();
            process.waitFor();
            Log.e("Up","got here6");
            process=Runtime.getRuntime().exec("chown root:root /system/priv-app/Police/Police.apk");
            new ReaderThread(process).start();
            process.waitFor();
            Log.e("Up","got here7");
            process=Runtime.getRuntime().exec("reboot");*/

            //"stop;mount -o rw,remount /system;mv /storage/emulated/0/police/app.apk /system/priv-app/Police/Police.apk;chmod 755 /system/priv-app/Police;chmod -R 644 /system/priv-app/Police;chmod 755 /system/priv-app/Police;reboot"
            //os.writeBytes("exit\n");
            //os.flush();
            //process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateTBPolice(String apkPath){
        try {

            process=Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            Log.e("Up","got here2");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            Log.e("Path",apkPath);
            os.writeBytes(//"stop;"+
                    "mount -o rw,remount /system;"+
                            "cp "+apkPath+" /system/app/Police.apk;"+
                            "rm "+apkPath+";"+
                            "chmod -R 644 /system/app/Police.apk;"+
                            "chown root:root /system/app/Police.apk;"+
                            "reboot\n");
            os.flush();
            Log.e("Up","got here3");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(String s){
        try {
            process=Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(s+"\n");
            os.flush();
            //os.writeBytes("exit\n");
            //os.flush();
            //process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (InterruptedException e) {
        //    e.printStackTrace();
        // }
    }


    @Override
    public void close() throws IOException {
        process.destroy();
    }
    private static class ReaderThread extends Thread{
        private Process process;
        private BufferedReader reader,errorReader;
        private ConsoleOut consoleOut;

        private ReaderThread(Process process) {
            this.process = process;
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        }

        @Override
        public void run() {
            // byte[] buffer = new byte[1024];
            StringBuilder errorBuilder = new StringBuilder();
            StringBuilder responseBuilder = new StringBuilder();
            String s;
            String e;
            int proccessCode=-69;
            while (true){
                try {
                    if (reader==null || errorReader == null) break;
                    //int length =process.getErrorStream().read(buffer);
                    //e = new String(buffer,0,length);
                    while ((s=reader.readLine())!=null){
                        Log.d("console","Console out:"+s);
                        responseBuilder.append(s);
                    }
                    while ((e=errorReader.readLine())!=null){
                        Log.e("console","Console error:"+e);
                        errorBuilder.append(e);
                    }
                    Thread.sleep(5);
                    proccessCode = process.exitValue();
                    Log.e("console","process code="+proccessCode);
                    break;

                }catch (IllegalThreadStateException err){

                }catch (InterruptedException err) {
                    err.printStackTrace();
                    break;
                }
                catch (Exception err) {
                    err.printStackTrace();
                    break;
                }
            }
            String out ="";
            if (errorBuilder.length()>0){
                out="Console Error: "+errorBuilder.toString();
            }
            if (responseBuilder.length()>0){
                if (!out.isEmpty()) out+="\r\n";
                out+="Console out: "+responseBuilder.toString();
            }

            if (consoleOut!=null) consoleOut.onCommandFinished(proccessCode,out);
        }

        public void setConsoleOut(ConsoleOut consoleOut) {
            this.consoleOut = consoleOut;
        }
    }
}
