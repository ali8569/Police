package ir.markazandroid.police.downloader;

import android.annotation.SuppressLint;

/**
 * Coded by Ali on 3/3/2019.
 */
public abstract class DefaultDownloadListener implements Downloader.DownloadListener {
    private double downloaded=0;
    @SuppressLint("DefaultLocale")
    @Override
    public void downloadStatus(Downloader.DownloadStatus status) {
        switch (status.getStatus()){
            case Downloader.DownloadStatus.STATUS_IDEAL:
                onProgress("Download Ideal"); break;

            case Downloader.DownloadStatus.STATUS_STARTED:
                onProgress("Download Started"); break;

            case Downloader.DownloadStatus.STATUS_RUNNING:
                double downloaded = status.getDownloadedSize()/1024f/1024f;
                double max = status.getMaxSize()/1024f/1024f;
                if (downloaded-this.downloaded>=0.2 || downloaded==0 || downloaded==status.getMaxSize()){
                    this.downloaded=downloaded;
                    onProgress(String.format("Downloaded %f of %f MB",downloaded,max)); break;
                }
                break;

            case Downloader.DownloadStatus.STATUS_CANCELED:
                onProgress("Download Cancelled"); break;

            case Downloader.DownloadStatus.STATUS_FAILED:
                onProgress("Download Failed, Reason="+status.getFailureException().getMessage()); break;

            case Downloader.DownloadStatus.STATUS_SUCCESS: {
                success(status);
                onProgress("Download Finished Successfully");
                break;
            }
        }
    }

    public void success(Downloader.DownloadStatus status){

    }

    public abstract void onProgress(String progress);
}
