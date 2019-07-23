package ir.markazandroid.police.util;

import android.content.Context;
import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import ir.markazandroid.police.downloader.DefaultDownloadListener;
import ir.markazandroid.police.downloader.Downloader;

/**
 * Coded by Ali on 3/6/2019.
 */
public class PackageManager {

    public static int PROGRESS_RUNNING=1;
    public static int PROGRESS_SUCCESS=2;
    public static int PROGRESS_FAILED=3;

    private File packageCacheDirectory;
    private Console console;
    private Context context;

    public interface PackageManagerListener{
        void process(int processCode,String status);
    }

    private Downloader downloader;

    public PackageManager(Downloader downloader, Context context, Console console) {
        this.downloader = downloader;
        packageCacheDirectory=new File(Environment.getExternalStorageDirectory()+"/police/pmcd");
        this.console = console;
        this.context=context;
    }

    public boolean downloadAndInstall(String url,PackageManagerListener listener,int method){
        File apk = new File(packageCacheDirectory,extractFilename(url));
        return downloader.download(url, apk, true, new DefaultDownloadListener() {
            @Override
            public void onProgress(String progress) {
                listener.process(PROGRESS_RUNNING,progress);
            }

            @Override
            public void success(Downloader.DownloadStatus status) {
                if (method==1)
                    installTaxiBoard(apk);
                else if (method == 2)
                    console.installInSystem(apk.getPath(), "Advertiser", "Advertiser.apk");
                else if (method == 3)
                    console.installInSystem(apk.getPath(), "Launcher", "Launcher.apk");
                else
                    install(apk);

               listener.process(PROGRESS_SUCCESS,"Installed");
            }
        });
    }

    public void install(File apk){
        console.write("pm install -r -d " + apk.getPath());
        //FileUtils.deleteQuietly(apk);
    }

    public void installTaxiBoard(File apk){
        console.wWait("pm enable com.softwinner.launcher");
        console.wWait("pm disable com.taxiboard.board");
        console.wWait("pm install -r -d "+apk.getPath());
        console.wWait("pm enable com.taxiboard.board");
        console.wWait("pm disable com.softwinner.launcher");
        //console.write("reboot");
        //FileUtils.deleteQuietly(apk);
    }

    public void installTaxiBoard(){
        try {
            File taxiboardApk=new File(Environment.getExternalStorageDirectory()+"/police/tapp.apk");
            FileUtils.copyInputStreamToFile(context.getAssets().open("Taxiboard_131.apk"),
                    taxiboardApk);
            installTaxiBoard(taxiboardApk);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //console.write("reboot");
        //FileUtils.deleteQuietly(apk);
    }


    private static String extractFilename(String fileUrl){
        return fileUrl.substring(fileUrl.lastIndexOf('/')+1);
    }

    private synchronized File getFile(String url) throws IOException {
        File dest = new File(packageCacheDirectory,"packageTemp"+Math.round(Math.random()*1000)+".apk");
        int tryCount =0;
        while (dest.exists()){
            tryCount++;
            dest = new File(packageCacheDirectory,"packageTemp"+Math.round(Math.random()*1000)+".apk");
            if (tryCount>1000) throw new IOException("CacheDirectory NA");
        }
        FileUtils.touch(dest);
        return dest;
    }

}
