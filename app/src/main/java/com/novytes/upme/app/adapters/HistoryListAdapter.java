package com.novytes.upme.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.novytes.upme.app.R;
import com.novytes.upme.app.models.ItemSugarRecord;

import java.util.List;

/**
 * Created by res on 6/8/14.
 * An adapter for the history list items
 */
public class HistoryListAdapter extends BaseAdapter {

    List<ItemSugarRecord> data;
    ViewGroup v;

    private Context context;

    public HistoryListAdapter(Context context, List<ItemSugarRecord> data){
        this.context = context;
        this.data = data;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ItemSugarRecord getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        v = viewGroup;
        final ItemSugarRecord item = getItem(i);
        final int position =  i;
        final ViewHolder holder;

        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.history_item, viewGroup, false);
            holder = new ViewHolder();
            holder.urlText = (TextView) view.findViewById(R.id.url);
            holder.localPathText = (TextView) view.findViewById(R.id.local_path);
            holder.toggleButton = (Button) view.findViewById(R.id.expandable_toggle_button);
            holder.deleteButton = (Button) view.findViewById(R.id.btn_delete);
            holder.shareButton = (Button) view.findViewById(R.id.btn_share);
            holder.copyBUtton = (Button) view.findViewById(R.id.btn_copy);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        holder.urlText.setText(item.longUrl);
        holder.localPathText.setText(item.localPath);

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, item.longUrl);
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        });

        holder.copyBUtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                            .getSystemService(context.CLIPBOARD_SERVICE);
                    clipboard.setText(item.longUrl);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                            .getSystemService(context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData
                            .newPlainText("url", item.longUrl);
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(context, "Link copied.", Toast.LENGTH_LONG).show();
            }
        });

        final View finalView = view;
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemSugarRecord uItem = ItemSugarRecord.findById(ItemSugarRecord.class, item.getId());
                uItem.delete();
                holder.toggleButton.performClick();
                removeListItem(finalView, position);

            }
        });


        return view;

    }

    /**
     *
     * Animates and removes an item from the list.
     *
     * @param  view     the view to be removed from the list
     * @param  position the position of the item in the list
     * @return          nothing
     *
     */
    protected void removeListItem(final View rowView, final int positon) {
        final Animation animation = AnimationUtils.loadAnimation(rowView.getContext(), R.anim.splash_fade_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

            @Override
            public void onAnimationEnd(Animation animation) {
                rowView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rowView.startAnimation(animation);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {

            @Override
            public void run() {
                data.remove(positon);
                notifyDataSetChanged();
                animation.cancel();
                rowView.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }


    static class ViewHolder {
        TextView urlText;
        TextView localPathText;
        Button toggleButton;
        Button deleteButton;
        Button shareButton;
        Button copyBUtton;
    }
}
