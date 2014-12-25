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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends ActionBarActivity {

    static LinkedBlockingQueue<String> queueOfURLS = new LinkedBlockingQueue<String>();
    static LinkedBlockingQueue<String> resultQueue = new LinkedBlockingQueue<String>();
    static LinkedBlockingQueue<String> toScaleQueue = new LinkedBlockingQueue<String>();

    public JSONObject parent;
    public ArrayList<String> links;

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

//        for (String url : links) {  // adding url to linked blocking queue
//            queueOfURLS.put(url);
//        }

        Bitmap pic, re_pic;
        pic = setOneBitmap(links.get(0));
        re_pic = getResizedBitmap(pic, 100, 100);
        ImageView img = (ImageView) findViewById(R.id.picsFromUrl);
        img.setImageBitmap(re_pic);
    }

    // --------------------------methods-------------------------------

    public static Bitmap setOneBitmap(String src){

            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
    }



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

        for(int i=0; i<m_jArry.length(); i++){
            JSONObject o = (JSONObject) m_jArry.get(i);
            urls.add(o.get("url").toString());
        }

        return urls;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }


}
