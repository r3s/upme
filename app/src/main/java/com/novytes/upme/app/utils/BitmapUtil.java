package com.novytes.upme.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;


import java.lang.ref.WeakReference;

/**
 * Created by res on 3/8/14.
 */
public class BitmapUtil {

    private Resources res;
    private Context context;
    private int resId;
    private final ImageView imgView;
    private  int type;
    private String fileName;


    public BitmapUtil(Context c, Resources r, int resId, ImageView imgView, int type, String fileName){
        this.context = c;
        this.res = r;
        this.resId = resId;
        this.imgView = imgView;
        this.type = type;
        this.fileName = fileName;
    }

    public void setProcessedBitmapAsBg(){
        BitmapWorkerTask task = new BitmapWorkerTask(imgView);
        task.execute(resId);
    }


    public Bitmap resizeBitmap(Bitmap bmp, String tempFile){
        return decodeSampledBitmapFromResource(res, 0, 1024,768, 1, tempFile);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight, int type, String fileName) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if(type == 0){
            BitmapFactory.decodeFile(fileName, options);
        }else {
            BitmapFactory.decodeResource(res, resId, options);
        }
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        if(type == 0){
            return BitmapFactory.decodeFile(fileName, options);
        }else {
            return BitmapFactory.decodeResource(res, resId, options);
        }
    }


    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            RenderScript rs = RenderScript.create(context);
            Bitmap bitmapOriginal;
            if(type == 0){
                bitmapOriginal = decodeSampledBitmapFromResource(res, data,100,100, 0, fileName);
            }else {
                bitmapOriginal = decodeSampledBitmapFromResource(res, data, 100, 100, 1, "");
            }
            final Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(8f);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmapOriginal);
            return bitmapOriginal;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
