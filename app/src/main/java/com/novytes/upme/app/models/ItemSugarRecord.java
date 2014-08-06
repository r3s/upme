package com.novytes.upme.app.models;

import com.orm.SugarRecord;

/**
 * Created by res on 5/8/14.
 */
public class ItemSugarRecord extends SugarRecord<ItemSugarRecord> {
    public String sId;
    public String longUrl;
    public String shortUrl;
    public String localPath;
    int viewLimit;



    public ItemSugarRecord(){

    }

    public ItemSugarRecord(String sId, String longUrl, String shortUrl, String localPath, int viewLimit){
        this.sId = sId;
        this.longUrl = longUrl;
        this.shortUrl = shortUrl;
        this.localPath = localPath;
        this.viewLimit = viewLimit;
    }

}
