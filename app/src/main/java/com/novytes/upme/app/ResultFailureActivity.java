package com.novytes.upme.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.novytes.upme.app.utils.BitmapUtil;

 /*
 * This activity shows the result failed message. The message contains
 * vague reason for the failure, and is obtained as intent from the
 * main activity.
 */

public class ResultFailureActivity extends ActionBarActivity {

    private ImageView bgView;
    private TextView failTextView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_failure);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo));
        actionBar.setHomeButtonEnabled(true);

        bgView = (ImageView) findViewById(R.id.main_bg);
        failTextView = (TextView) findViewById(R.id.fail_text);
        backButton = (Button) findViewById(R.id.btn_back);

        BitmapUtil bmpUtil = new BitmapUtil(getApplication(), getResources(), R.drawable.large_bg, bgView, 1, "");
        bmpUtil.setProcessedBitmapAsBg();

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        failTextView.setText(message);

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
}
