package com.novytes.upme.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by res on 6/8/14.
 * Class providing network related utility functions
 *
 */
public class NetworkUtils {

    public NetworkUtils(){

    }

    /**
     *
     * Method to check if the network(internet) is available or not
     *
     * @param  context  context of the application
     * @return          true if network is available, otherwise false
     *
     */
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }

        return false;
    }
}
