package com.novytes.upme.app;


/*
*
* This class has been heavily commented out to disable the url shortening service.
* The feature will be made available once the library handles gzip inflation properly.
*
* */


import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.novytes.upme.app.models.ItemSugarRecord;
import com.novytes.upme.app.utils.BitmapUtil;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;


 /*
 * This activity is responsible for showing the result.
 * It receives the necessary data from the MainActivity through intent,
 * shows the URL for the result and provides the share buttons.
 *
 * This activity has been heavily commented out till the
 * android-async-http provides robust gzip inflation, because
 * goo.gl url shortening fails otherwise.
 *
 */


public class ResultSuccessActivity extends ActionBarActivity {

    private ImageView bgView;
    private TextView directUrlView;
    private Button shareButton1;
    private Button copyButton1;
    private Button backButton;
//    private Button shareButton2;
//    private Button copyButton2;
//    private TextView shorturlView;
    private ProgressBar progressBar;

    private String id;
    private String selectedFilePath;
    private int viewLimit;

    private String VIEW_URL = "http://novytes.com/t11/public/view/";
    private String longUrl;
//    private String shortUrl;
//    private AsyncHttpClient client;
//    private final static String SHORT_URL_API = "https://www.googleapis.com/urlshortener/v1/url";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_success);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo));
        actionBar.setHomeButtonEnabled(true);

        bgView = (ImageView) findViewById(R.id.main_bg);
        directUrlView = (TextView) findViewById(R.id.direct_url);
        shareButton1 = (Button) findViewById(R.id.btn_share1);
        copyButton1 = (Button) findViewById(R.id.btn_copy1);
//        shorturlView = (TextView) findViewById(R.id.short_url);
//        shareButton2 = (Button) findViewById(R.id.btn_share2);
//        copyButton2 = (Button) findViewById(R.id.btn_copy2);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        backButton = (Button) findViewById(R.id.btn_back);


//        shorturlView.setVisibility(View.INVISIBLE);
//        shareButton2.setVisibility(View.INVISIBLE);
//        copyButton2.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
//        message = intent.getStringExtra("message");
        selectedFilePath = intent.getStringExtra("file");
        viewLimit = intent.getIntExtra("limit", 5);

        BitmapUtil bmpUtil = new BitmapUtil(getApplication(), getResources(), R.drawable.large_bg, bgView, 0, selectedFilePath);
        bmpUtil.setProcessedBitmapAsBg();
//        client = new AsyncHttpClient();
//        client.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        manageResult();

        shareButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, longUrl);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share URL"));
            }
        });


        copyButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", longUrl);
                clipBoard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Link copied", Toast.LENGTH_LONG).show();
            }
        });

//        shareButton2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, longUrl);
//                sendIntent.setType("text/plain");
//                startActivity(Intent.createChooser(sendIntent, "Share URL"));
//            }
//        });
//
//
//        copyButton2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("url", longUrl);
//                clipBoard.setPrimaryClip(clip);
//
//                Toast.makeText(getApplicationContext(), "Link copied", Toast.LENGTH_LONG).show();
//            }
//        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    public void manageResult(){

        longUrl =VIEW_URL+id;
        directUrlView.setText(longUrl);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean save = preferences.getBoolean("pref_save_history", true);
        Log.d("UPME", "shared preference returned true" + save);
        if(save){
            saveToDB(false);
        }

//        JSONObject urlObject = new JSONObject();
//        try {
//            urlObject.put("longUrl",longUrl);
//            StringEntity entity = new StringEntity(urlObject.toString());
//            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//            Log.d("AOOO,", entity.getContentType().toString());
//            client.post(getApplicationContext(), SHORT_URL_API, entity, "application/json", shortUrlHandler);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

    }


    public void saveToDB(boolean short_url_present){
        if(short_url_present) {
//            ItemSugarRecord item = new ItemSugarRecord(id, longUrl, shortUrl, selectedFilePath, viewLimit);
//            item.save();
        }else{
            ItemSugarRecord item = new ItemSugarRecord(id, longUrl, "", selectedFilePath, viewLimit);
            item.save();
        }

    }

//
//    final AsyncHttpResponseHandler shortUrlHandler = new AsyncHttpResponseHandler() {
//        @Override
//        public void onSuccess(int i, Header[] headers, byte[] bytes) {
//            String jsonString = new String(bytes);
//            Log.d("APPPPUPME", jsonString);
//            try {
//
//                JSONObject jsonObject = new JSONObject(jsonString);
//                String id = jsonObject.getString("id");
//
//                if(id!=null){
//                    Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG ).show();
//                    shorturlView.setVisibility(View.INVISIBLE);
//                    shareButton2.setVisibility(View.INVISIBLE);
//                    copyButton2.setVisibility(View.INVISIBLE);
//                    progressBar.setVisibility(View.INVISIBLE);
//                    shortUrl = id;
//                    saveToDB(true);
//
//                }else {
//                    Toast.makeText(getApplicationContext(), "Unable to shorten url. noid", Toast.LENGTH_LONG).show();
//                    shorturlView.setText("Unable to get short url.");
//                    shorturlView.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.INVISIBLE);
//                    saveToDB(false);
//
//                }
//
//            } catch (JSONException e) {
//                if(headers != null){
//                    for(Header header: headers){
//                        Log.d("UPMEE", i+ " " +header.getValue());
//                    }
//                }
//                InputStream is = new ByteArrayInputStream(bytes);
//                try {
//                    GZIPInputStream gzip = new GZIPInputStream(is);
//                    int byteCount = 0;
//                    StringBuffer unGzipRes = new StringBuffer();
//                    byte[] buff = new byte[1024];
//                    while ((byteCount = gzip.read(buff, 0, 1024)) > 0) {
//                        // only append the buff elements that
//                        // contains data
//                        unGzipRes.append(new String(Arrays.copyOf(
//                                buff, byteCount), "utf-8"));
//                    }
//                    Log.d("UPMEE", unGzipRes.toString());
//
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//                Toast.makeText(getApplicationContext(), "Unable to shorten url. xcpt", Toast.LENGTH_LONG).show();
//                shorturlView.setText("Unable to get short url.");
//                shorturlView.setVisibility(View.VISIBLE);
//                progressBar.setVisibility(View.INVISIBLE);
//                saveToDB(false);
//            }
//        }
//
//        @Override
//        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//            if(headers != null){
//                for(Header header: headers){
//                    Log.d("UPMEE", i+ " " +header.getValue());
//                }
//            }
//
//            try {
//                InputStream is = new ByteArrayInputStream(bytes);
//                GZIPInputStream gzip = new GZIPInputStream(is);
//                int byteCount = 0;
//                StringBuffer unGzipRes = new StringBuffer();
//                byte[] buff = new byte[1024];
//                while ((byteCount = gzip.read(buff, 0, 1024)) > 0) {
//                    // only append the buff elements that
//                    // contains data
//                    unGzipRes.append(new String(Arrays.copyOf(
//                            buff, byteCount), "utf-8"));
//                }
//                Log.d("UPMEE", unGzipRes.toString());
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            String jsonString = new String(bytes);
//            Log.d("APPPPUPME", jsonString);
//            Toast.makeText(getApplicationContext(), "Unable to shorten url.", Toast.LENGTH_LONG).show();
//            shorturlView.setText("Unable to get short url. Fail");
//            shorturlView.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.INVISIBLE);
//            saveToDB(false);
//        }
//    };

}
