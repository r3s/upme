package com.novytes.upme.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.novytes.upme.app.utils.BitmapUtil;
import com.novytes.upme.app.utils.FilenameUtils;
import com.novytes.upme.app.utils.NetworkUtils;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*

* The main activity, showing the main upload screen.
* This is the parent of all other activities
*
* */
public class MainActivity extends ActionBarActivity implements NumberPickerDialogFragment.NumberPickerDialogHandler {

    /*Views in the layout*/
    /*TODO: Use a view injector to make life easier*/
    private ImageView bgView;
    private Button btnSelectImage;
    private Button btnViews;
    private CheckBox checkResize;
    private Button btnUpload;
    private ProgressBar progressBar;
    private Button btnHistory;
    private TextView t1, t2, t3;

    // Intent result code. Just a random number
    private int IMAGE_PICKER_SELECT = 12;

    // Options relating to upload
    private String selectedFilePath;
    private int viewLimit = 5;

    // Variables related to the network request
    /*TODO: Implement API key in the backend and add it here*/
    private static final String API_URL = "http://novytes.com/t11/public/form-submit";
    private static final String PARAM_API = "api";
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_FILE = "file";
    private static final String API_KEY = "";
    private static final String VALUE_API = "v1";
    private AsyncHttpClient client;

    // Variables related to the upload notification
    private static final int NOTIFICATION_ID = 3;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set icon for our actionbar
        getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo));

        bgView = (ImageView) findViewById(R.id.main_bg);
        btnSelectImage = (Button) findViewById(R.id.btn_choose_image);
        btnViews = (Button) findViewById(R.id.view_limit);
        checkResize = (CheckBox) findViewById(R.id.resize_checkbox);
        btnUpload = (Button) findViewById(R.id.upload_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        btnHistory = (Button) findViewById(R.id.history_button);

        t1 = (TextView) findViewById(R.id.choose_image_text);
        t2 = (TextView) findViewById(R.id.num_views_text);
        t3 = (TextView) findViewById(R.id.resize_text);
        progressBar.setVisibility(View.INVISIBLE);

        btnViews.setText(""+viewLimit);

        context = getApplicationContext();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Set the background image
        BitmapUtil bmpUtil = new BitmapUtil(getApplication(), getResources(), R.drawable.large_bg, bgView, 1, "");
        bmpUtil.setProcessedBitmapAsBg();

        // Start image picker on button click
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, IMAGE_PICKER_SELECT);
            }
        });

        // Start a number picker dialog on button click
        btnViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.MyCustomBetterPickerTheme)
                        .setPlusMinusVisibility(View.INVISIBLE)
                        .setDecimalVisibility(View.INVISIBLE);
                npb.setMinNumber(1);
                npb.setMaxNumber(1000);
                npb.show();
            }
        });

        // Check everything and start upload on button click
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFilePath != null){
                    File file;

                    /*If resize is checked, resize the bitmap, write to a cache file
                    * and upload that file
                    */
                    if(checkResize.isChecked()){
                        File cacheDir = getBaseContext().getCacheDir();
                        Bitmap newBitmap = BitmapUtil
                                            .decodeSampledBitmapFromResource(getResources(), 0,
                                                    1024,768,0,selectedFilePath);
                        file = new File(cacheDir, "tempimg");
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(file);
                            newBitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    95, out);
                            out.flush();
                            out.close();
                            doUpload(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(context,
                                    "Sorry, unable to access the file",
                                    Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(context,
                                    "Sorry, unable to access the file",
                                    Toast.LENGTH_LONG).show();
                        }

                    }else{

                        file = new File(selectedFilePath);
                        doUpload(file);
                    }

                }else{
                    Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Start history activity on button click
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, HistoryActivity.class);
                startActivity(i);
            }
        });

    }

    /**
     *
     * Sets all the required parameters and start an asynchronous http client
     *
     * @param  file  file to be uploaded
     * @return void
     *
     */
    private void doUpload(File file) {

        RequestParams params = new RequestParams();
        try {


            if(!NetworkUtils.isNetworkAvailable(context)){
                Toast.makeText(context, "Please check your network", Toast.LENGTH_LONG).show();
                return;
            }

            params.add(PARAM_API, VALUE_API);
            params.add(PARAM_LIMIT, String.valueOf(viewLimit));
            params.put(PARAM_FILE, file, "image/*");

            notificationBuilder = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setSmallIcon(android.R.drawable.ic_menu_upload)
                    .setContentTitle("UpMe")
                    .setContentText("Uploading file...");
            notificationBuilder.setProgress(100, 0, false);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            client = new AsyncHttpClient();
            client.post(API_URL, params, httpHandler);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "Sorry, unable to access the file",
                    Toast.LENGTH_LONG).show();
        }


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
            Intent i = new Intent(context, SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_SELECT && resultCode == Activity.RESULT_OK) {

            // Get the image file path
            selectedFilePath = getBitmapPath(data, context);
            t1.setText(FilenameUtils.getName(selectedFilePath));
            setSelectedImage(selectedFilePath);
        }
    }

    /**
     *
     * Returns the path of the image specified in the intent provided
     *
     * @param  data     the intent obtained from the image picker activity
     * @param  context  context of the application
     * @return          path of the image specified in the intent
     *
     */
    public static String getBitmapPath(Intent data, Context context){
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }



    /**
     *
     * Sets the image specified in the argument as the main background
     *
     * @param  filePath     the path of the image to be set as the selected file
     * @return              nothing.
     *
     */
    public void setSelectedImage(String filePath){
        BitmapUtil bmpUtil = new BitmapUtil(getApplication(), getResources(), R.drawable.large_bg, bgView, 0, filePath);
        bmpUtil.setProcessedBitmapAsBg();
    }

    /**
     *
     * Reset all the views before starting the result related activities, so that
     * it appears as everything is reset.
     *
     * @return              nothing.
     *
     */

    public void resetViews(){
        // decode using resource id. Put 5th argument (type = 1)
        BitmapUtil bmpUtil = new BitmapUtil(getApplication(), getResources(), R.drawable.large_bg, bgView, 1, "");
        bmpUtil.setProcessedBitmapAsBg();

        t1.setText("Choose Image");

        progressBar.setVisibility(View.INVISIBLE);
        btnUpload.setVisibility(View.VISIBLE);
        checkResize.setChecked(false);
        selectedFilePath = null;

        viewLimit = 5;
        btnViews.setText(""+viewLimit);
    }


    @Override
    public void onDialogNumberSet(int i, int i2, double v, boolean b, double v2) {
        if(i2>0){
            btnViews.setText(""+i2);
            viewLimit = i2;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    /**
     *
     * The asynchronous http handler, handling the upload post request.
     *
     */
    final AsyncHttpResponseHandler httpHandler = new AsyncHttpResponseHandler() {

        @Override
        public void onStart() {
            super.onStart();
            // Show the progress bar and hide the upload button
            btnUpload.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        public void onProgress(int bytesWritten, int totalSize) {

            /*
            * Get the percentage of progress and update the notification
            * progress bar every 10%
            */
            int progressPercentage = (int)100*bytesWritten/totalSize;

            if(progressPercentage<100 && progressPercentage%10 == 0){

                notificationBuilder.setProgress(100, progressPercentage, false);
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }

            Log.d("UPME", bytesWritten + " of "+ totalSize);
        }

        /*
        * Check the response, decode it to a JSON object, see if the status is success
        * and then start the appropriate activity. Also set the notification progress
        * bar finished status
        */
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {

            int vL = viewLimit;
            String path =  selectedFilePath;
            resetViews();

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            Intent historyIntent = new Intent(context, HistoryActivity.class);
            stackBuilder.addParentStack(ResultSuccessActivity.class);
            stackBuilder.addNextIntent(historyIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            String jsonString = new String(bytes);

            try {

                JSONObject jsonObject = new JSONObject(jsonString);
                String id = jsonObject.getString("id");
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");

                if(status.equals("success")){

                    notificationBuilder.setContentText("Upload finished.")
                                        .setProgress(0,0,false)
                                        .setContentIntent(resultPendingIntent);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

                    Intent intent = new Intent(context, ResultSuccessActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("message", message);
                    intent.putExtra("file", path);
                    intent.putExtra("limit", vL);
                    startActivity(intent);
                }else{

                    notificationBuilder.setContentText("Upload failed.")
                            .setProgress(0,0,false);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

                    Intent intent = new Intent(context, ResultFailureActivity.class);
                    intent.putExtra("message", message);
                    startActivity(intent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                notificationBuilder.setContentText("Upload failed.")
                        .setProgress(0,0,false);
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                Intent intent = new Intent(context, ResultFailureActivity.class);
                intent.putExtra("message", "There was a problem with the network. Please go back and retry.");
                startActivity(intent);
            }

        }

        // Start the ResultFailureActivity, and set the notification progress bar text as failed
        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            resetViews();
            notificationBuilder.setContentText("Upload failed.")
                                .setProgress(0,0,false);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            Intent intent = new Intent(context, ResultFailureActivity.class);
            intent.putExtra("message", "Unable to connect to the server. Please go back and retry.");
            startActivity(intent);
        }
    };


}
