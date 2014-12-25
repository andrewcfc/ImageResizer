package com.example.imageresizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends ActionBarActivity {

    static LinkedBlockingQueue<String> queueOfURLS = new LinkedBlockingQueue<String>();
    static LinkedBlockingQueue<String> resultQueue = new LinkedBlockingQueue<String>();
    static LinkedBlockingQueue<String> toScaleQueue = new LinkedBlockingQueue<String>();

    public JSONObject parent;
    public ArrayList<String> links;
    public ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            startScalingProgram();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    //---------------------------main-method---------------------------

    public void startScalingProgram() throws JSONException, InterruptedException {

        links = parseJSON();  // parsing

        for (String url : links) {  // adding url to linked blocking queue
            queueOfURLS.put(url);
        }

        ArrayList<DownloadingThread> downloadingThreads = startDownloadingThreads(3);

        Bitmap testImg;
        testImg = pictures.get(0);


        ImageView imgView = (ImageView) findViewById(R.id.picsFromUrl);
        imgView.setImageBitmap(testImg);

        killDownloadingThreads(downloadingThreads);
    }

    //---------------------------threads and methods for them----------------------

    public class DownloadingThread extends Thread {

        @Override
        public void run() {

            while (true) {
                String url;
                try {
                    url = queueOfURLS.take();
                } catch (InterruptedException e) {
                    return;
                }

                downloadUrl(url);

                try {
                    toScaleQueue.put(url);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private ArrayList<DownloadingThread> startDownloadingThreads(int count) {
        ArrayList<DownloadingThread> r = new ArrayList<DownloadingThread>();

        for (int i = 0; i < count; i++) {
            DownloadingThread t = new DownloadingThread();
            t.start();
            r.add(t);
        }

        return r;
    }

    // --------------------------methods-------------------------------

    public void downloadUrl(String src) {

        InputStream is = null;
        BufferedInputStream bis = null;
        Bitmap bmp = null;

        try {
            URL url = new URL(src);
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is);
            bmp = BitmapFactory.decodeStream(bis);

        } catch (MalformedURLException e) {

        } catch (IOException e) {

        } catch (Exception e) {

        } finally {
            try {
                if (is != null)
                    is.close();
                if (bis != null)
                    bis.close();
            } catch (IOException e) {

            }
        }
        pictures.add(bmp);
    }


//    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // CREATE A MATRIX FOR THE MANIPULATION
//        Matrix matrix = new Matrix();
//        // RESIZE THE BIT MAP
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        // "RECREATE" THE NEW BITMAP
//        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
//                matrix, false);
//
//        return resizedBitmap;
//    }


    public ArrayList<String> parseJSON() throws JSONException {
        String json = null;
        try {

            InputStream is = getAssets().open("lampard.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        JSONObject JSObj = new JSONObject(json);

        ArrayList<String> urls = new ArrayList<String>();
        JSONArray m_jArry = JSObj.getJSONArray("URLS");

        for (int i = 0; i < m_jArry.length(); i++) {
            JSONObject o = (JSONObject) m_jArry.get(i);
            urls.add(o.get("url").toString());
        }

        return urls;
    }


    private static void killDownloadingThreads(ArrayList<DownloadingThread> downloadingThreads) {
        for (DownloadingThread t : downloadingThreads) {
            t.interrupt();
        }
    }

}
