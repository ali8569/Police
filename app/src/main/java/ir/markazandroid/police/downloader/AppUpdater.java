package ir.markazandroid.police.downloader;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.BuildConfig;
import ir.markazandroid.police.network.JSONParser.Parser;
import ir.markazandroid.police.network.NetStatics;
import ir.markazandroid.police.object.Version;
import ir.markazandroid.police.util.Console;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Coded by Ali on 5/24/2018.
 */
public class AppUpdater {

    public interface UpdateProgressListener{
        void onStart();
        void onProgress(String progress);
    }

    private Downloader downloader;
    private File appFile;
    private Console console;
    public AppUpdater(Downloader downloader,Console console) {
        this.console=console;
        this.downloader=downloader;
        appFile = new File(Environment.getExternalStorageDirectory()+"/police/app.apk");
    }
    public boolean update(Version version,UpdateProgressListener listener){
        if (version.getAllowDowngrade() || version.getVersion() > BuildConfig.VERSION_CODE) {
            boolean started = downloader.download(version.getUrl(), appFile, false, new DefaultDownloadListener() {
                @Override
                public void onProgress(String progress) {
                    listener.onProgress(progress);
                }

                @Override
                public void downloadStatus(Downloader.DownloadStatus status) {
                    super.downloadStatus(status);
                    if (status.getStatus()==Downloader.DownloadStatus.STATUS_SUCCESS) {
                        onProgress("Going to Install");
                        installSystemUpdate(version);
                    }
                }
            });
            if (started) listener.onStart();
            return true;
        }
        return false;
    }


    private void installSystemUpdate(Version version){
       /* String path = "/storage/emulated/0/police/app.apk";
        File file = new File("/storage/emulated/0/police/app.apk");
        if (!file.exists())
            path="/storage/emulated/legacy/police/app.apk";
*/
        if ("TB".equalsIgnoreCase(version.getModel()))
            console.updateTBPolice(appFile.getPath());
        else
            console.updatePolice(appFile.getPath());
    }

    private void installUpdate() {
        String path = "/storage/emulated/0/police/app.apk";
        File file = new File("/storage/emulated/0/police/app.apk");
        if (!file.exists())
            path="/storage/emulated/legacy/police/app.apk";

        Console console = new Console();
        //console.start();
        console.update("pm install -d -r "+path+";reboot");
        //if (isTouchDisabled())
          //  console.update("pm install -d -r "+path+";reboot");
        //else
          //  console.update("pm install -d -r "+path+";su -e monkey -p ir.markazandroid.police -c android.intent.category.LAUNCHER 1");
        //console.write("");

    }

    public static class GenericFileProvider extends FileProvider {

    }



}
