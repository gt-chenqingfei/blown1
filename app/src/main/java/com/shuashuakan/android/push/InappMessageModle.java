package com.shuashuakan.android.push;

import org.json.JSONObject;


public class InappMessageModle {

    public InappMessageModle(){}

    //文案
    public String content;

    //图片url 左
    public String icon_url;

    //图片url 右
    public String cover_url;

    //链接地址
    public String redirect_url;

    //过期时间
    public long expire_at;

    public static InappMessageModle parsing(JSONObject data){
        if(data == null)
            return null;
        InappMessageModle modle = new InappMessageModle();
        modle.expire_at    = data.optLong("expire_at");
        modle.content      = data.optString("content" );
        modle.icon_url     = data.optString("icon_url");
        modle.cover_url    = data.optJSONObject("link").optString("cover_url"   );
        modle.redirect_url = data.optJSONObject("link").optString("redirect_url");
        return modle;
    }

}
