package ir.markazandroid.police.downloader;

import android.content.Context;
import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ir.markazandroid.police.network.NetworkClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Coded by Ali on 3/3/2019.
 */
public class Downloader{

    public interface DownloadListener{
        void downloadStatus(DownloadStatus status);
    }


    private NetworkClient client;
    private Executor executor;
    private ConcurrentHashMap<String,DownloadTask> tasks;
    private Context context;

    public Downloader(Context context,NetworkClient client) {
        this.context=context;
        this.client = client;
        executor=Executors.newCachedThreadPool();
        tasks=new ConcurrentHashMap<>();

    }

    public synchronized boolean download(String url,File des,boolean retryOnFailure,DownloadListener listener){
        if (tasks.get(url)!=null) return false;

        DownloadTask downloadTask = new DownloadTask(url,des,listener);
        downloadTask.setKeepRetrying(retryOnFailure);
        tasks.put(url,downloadTask);
        executor.execute(downloadTask);
        return true;
    }

    private void taskFinished(String url){
        tasks.remove(url);
    }

    public boolean cancelDownload(String url){
        DownloadTask downloadTask =tasks.get(url);
        if (downloadTask!=null) {
            downloadTask.setCancelled(true);
            return true;
        }
        return false;
    }



    private class DownloadTask implements Runnable{

        private String url;
        private File desPath;
        private DownloadListener listener;
        private DownloadStatus downloadStatus;
        private volatile boolean isRunning;
        private volatile boolean keepRetrying;
        private volatile boolean isCancelled;

        private DownloadTask(String url, File desPath, DownloadListener listener) {
            this.url = url;
            this.desPath = desPath;
            this.listener = listener;
            downloadStatus=new DownloadStatus();
            downloadStatus.url=url;
            downloadStatus.path=desPath;
            downloadStatus.status=DownloadStatus.STATUS_IDEAL;
            broadcast();
        }

        @Override
        public void run() {
            isRunning=true;
            downloadStatus.status=DownloadStatus.STATUS_STARTED;
            broadcast();

            do {
                makeRequest();
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (downloadStatus.status==DownloadStatus.STATUS_FAILED && !isCancelled && keepRetrying);

            isRunning=false;
            taskFinished(url);
            downloadStatus=null;
            listener=null;

        }

        private void makeRequest(){
            Response response=null;
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                response = client.getClient().newCall(request).execute();

                downloadStatus.maxSize=response.body().contentLength();
                downloadStatus.status=DownloadStatus.STATUS_RUNNING;
                broadcast();
                download(response.body().byteStream());
                if (isCancelled){
                    downloadStatus.status=DownloadStatus.STATUS_CANCELED;
                }
                else {
                    downloadStatus.status = DownloadStatus.STATUS_SUCCESS;
                }
                broadcast();
            }
            catch (Exception e){
                downloadStatus.status=DownloadStatus.STATUS_FAILED;
                downloadStatus.failureException=e;
                broadcast();
            }finally {
                if (response!=null)
                    response.close();
            }
        }

        private void download(InputStream inputStream) throws IOException {
            File temp = requestNewTempFile();
            copyLarge(inputStream,temp);
            if (isCancelled) {
                FileUtils.deleteQuietly(temp);
            }
            else {
                if (desPath.exists())
                    FileUtils.forceDelete(desPath);
                FileUtils.moveFile(temp, desPath);
            }
        }

        private void copyLarge(final InputStream input,File des) throws IOException {
            OutputStream output = FileUtils.openOutputStream(des);
            byte[] buffer = new byte[1024 * 4];
            long count = 0;
            int n;
            while (-1 != (n = input.read(buffer))) {
                if (isCancelled){
                    output.close();
                    return;
                }
                output.write(buffer, 0, n);
                count += n;
                downloadStatus.downloadedSize=count;
                broadcast();
            }
            output.flush();
            output.close();
        }

        private void broadcast(){
            if (listener!=null){
                listener.downloadStatus(downloadStatus);
            }
        }

        public void setCancelled(boolean cancelled) {
            isCancelled = cancelled;
        }

        public void setKeepRetrying(boolean keepRetrying) {
            this.keepRetrying = keepRetrying;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public boolean isKeepRetrying() {
            return keepRetrying;
        }

        public boolean isCancelled() {
            return isCancelled;
        }
    }

    private synchronized File requestNewTempFile() throws IOException {
        File cache = context.getCacheDir();
        File dest = new File(cache,"downloaderTemp"+Math.round(Math.random()*1000)+".tmp");
        int tryCount =0;
        while (dest.exists()){
            tryCount++;
            dest = new File(cache,"downloaderTemp"+Math.round(Math.random()*1000)+".tmp");
            if (tryCount>1000) throw new IOException("CacheDirectory NA");
        }
        FileUtils.touch(dest);
        return dest;
    }

    public static class DownloadStatus implements Serializable{
        public static final int STATUS_IDEAL=-1;
        public static final int STATUS_STARTED=1;
        public static final int STATUS_RUNNING=2;
        public static final int STATUS_SUCCESS=3;
        public static final int STATUS_FAILED=4;
        public static final int STATUS_CANCELED=5;

        private String url;
        private File path;
        private int status;
        private long maxSize;
        private long downloadedSize;
        private Exception failureException;

        public String getUrl() {
            return url;
        }

        public File getPath() {
            return path;
        }

        public int getStatus() {
            return status;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public long getDownloadedSize() {
            return downloadedSize;
        }

        public Exception getFailureException() {
            return failureException;
        }
    }

}
