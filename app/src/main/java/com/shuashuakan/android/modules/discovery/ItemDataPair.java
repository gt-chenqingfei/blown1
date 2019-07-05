package com.shuashuakan.android.modules.discovery;


import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by hanguisen on 2018/1/15.
 */

public class ItemDataPair implements MultiItemEntity {

    private Object data;
    private int type;

    public ItemDataPair(Object data, int type) {
        this.data = data;
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object object) {
        this.data = object;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getItemType() {
        return type;
    }

}
