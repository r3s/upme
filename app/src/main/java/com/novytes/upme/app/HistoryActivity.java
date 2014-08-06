package com.novytes.upme.app;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import com.novytes.upme.app.adapters.HistoryListAdapter;
import com.novytes.upme.app.models.ItemSugarRecord;
import com.novytes.upme.app.utils.BitmapUtil;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

 /*
 * This activity shows a list of items which the user has previously
  * uploaded. It also provides buttons for removing/sharing the items
 */


public class HistoryActivity extends ActionBarActivity {


    private ListView historyList;
    private ImageView bgView;

    private List<ItemSugarRecord> historyListItems;
    private HistoryListAdapter hAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(getResources().getDrawable(R.drawable.ic_ab_history));
        actionBar.setHomeButtonEnabled(true);

        historyList = (ListView) findViewById(R.id.list);
        bgView = (ImageView) findViewById(R.id.main_bg);

        historyListItems = new ArrayList<ItemSugarRecord>();
        hAdapter = new HistoryListAdapter(getApplicationContext(), historyListItems);


        // decode using resource id. Put 5th argument (type = 1)
        BitmapUtil bmpUtil = new BitmapUtil(getApplication(), getResources(), R.drawable.large_bg, bgView, 1, "");
        bmpUtil.setProcessedBitmapAsBg();

        historyList.setAdapter(
                new SlideExpandableListAdapter(
                        hAdapter,
                        R.id.expandable_toggle_button,
                        R.id.expandable
                )
        );
        new RetrieveItemstask().execute();

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


    private class RetrieveItemstask extends AsyncTask<String, Void, List<ItemSugarRecord>> {

        @Override
        protected List<ItemSugarRecord> doInBackground(String... params) {

            return ItemSugarRecord.listAll(ItemSugarRecord.class);
        }

        @Override
        protected void onPostExecute(List<ItemSugarRecord> data) {
            historyListItems.clear();
            historyListItems.addAll(data);
            hAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {}
    }
}

