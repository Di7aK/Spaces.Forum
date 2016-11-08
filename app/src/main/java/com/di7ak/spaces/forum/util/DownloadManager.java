package com.di7ak.spaces.forum.util;

import android.os.AsyncTask;
import android.os.Environment;
import android.webkit.URLUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.client.methods.HttpGet;

public class DownloadManager {
    public static final int HTTP_TIMEOUT = 10000;
    private static HashMap<String, List<DownloadListener>> downloads
    = new HashMap<String, List<DownloadListener>>();
    private static HashMap<String, AsyncTask<String, Integer, File>> tasks
    = new HashMap<String, AsyncTask<String, Integer, File>>();
    public static File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static HttpClient mHttpClient;

    public static void download(final String url, DownloadListener listener, final File customFile) {
        downloads.put(url, new ArrayList<DownloadListener>());
        downloads.get(url).add(listener);
        AsyncTask<String, Integer, File> task = new  AsyncTask<String, Integer, File>() {

            protected File doInBackground(String... urls) {
                try {
                    String fileName = URLUtil.guessFileName(urls[0], null, null);

                    File file = customFile == null ? new File(downloadsDirectory, fileName.replaceAll("-spaces.ru", "")) : customFile;

                    HttpClient client = getHttpClient();
                    HttpGet request = new HttpGet(urls[0]);

                    HttpResponse response = client.execute(request); 

                    FileOutputStream fileOutput = new FileOutputStream(file);

                    InputStream inputStream = response.getEntity().getContent();

                    int totalSize = (int)response.getEntity().getContentLength();
                    int downloadedSize = 0;

                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        publishProgress(downloadedSize, totalSize);
                        if (isCancelled()) break;
                    }
                    fileOutput.close();

                    return file;
                } catch (MalformedURLException e) {
                    android.util.Log.e("lol", "", e);
                    return null;
                } catch (IOException e) {
                    android.util.Log.e("lol", "", e);
                    return null;
                }
            }

            protected void onProgressUpdate(Integer... progress) {
                if (!isCancelled()) {
                    List<DownloadListener> listeners = downloads.get(url);
                    if(listeners != null) {
                        for (DownloadListener listener : listeners) {
                            listener.onProgress(progress[0], progress[1]);
                        }
                    }
                }
            }

            protected void onPostExecute(File result) {
                if (!isCancelled()) {
                    List<DownloadListener> listeners = downloads.get(url);
                    for (DownloadListener listener : listeners) {
                        if (result != null) listener.onSuccess(result);
                        else listener.onError();
                    }
                    downloads.remove(url);
                    tasks.remove(url);
                }
            }
        };
        tasks.put(url, task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    public static void cancellDownload(String url) {
        if (tasks.containsKey(url)) {
            tasks.get(url).cancel(true);
            tasks.remove(url);
            downloads.remove(url);
        }
    }

    public static boolean isDownload(String url) {
        return downloads.containsKey(url);
    }

    public static void appendListener(String url, DownloadListener listener) {
        if (downloads.containsKey(url)) {
            downloads.get(url).add(listener);
        }
    }

    private static HttpClient getHttpClient() {
        mHttpClient = new DefaultHttpClient();
        final HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
        ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
        return mHttpClient;     
    }

    public interface DownloadListener {

        public void onProgress(int downloaded, int total);

        public void onSuccess(File file);

        public void onError();
    }
}
