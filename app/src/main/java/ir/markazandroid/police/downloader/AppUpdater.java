package ir.markazandroid.police.downloader;

import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

import ir.markazandroid.police.BuildConfig;
import ir.markazandroid.police.object.Version;
import ir.markazandroid.police.util.Console;

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

    }

    public static class GenericFileProvider extends FileProvider {

    }



}
