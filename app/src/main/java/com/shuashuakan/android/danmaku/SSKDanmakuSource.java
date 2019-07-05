package com.shuashuakan.android.danmaku;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import master.flame.danmaku.danmaku.parser.IDataSource;


/**
 *@author: zhaoningqiang
 *@time: 2019/5/6
 *@Description:
 */
public class SSKDanmakuSource implements IDataSource<JSONArray>{
	private JSONArray mJSONArray;

	public SSKDanmakuSource(JSONArray jsonArray) {
		mJSONArray = jsonArray;
	}

    public JSONArray data(){
    	return mJSONArray;
    }

	@Override
	public void release() {
		mJSONArray = null;
	}

}
