/*
 * Copyright (C) 2013 Chen Hui <calmer91@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shuashuakan.android.danmaku;

import android.graphics.Color;

import com.shuashuakan.android.event.DanmakaSendEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Duration;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import timber.log.Timber;

import static master.flame.danmaku.danmaku.model.IDanmakus.ST_BY_TIME;

/**
 * @author: zhaoningqiang
 * @time: 2019/5/6
 * @Description:
 */
public class SSKDanmakuParser extends BaseDanmakuParser {
    private int textColor = 0xffffff;
    private int sendBarrageTextColor = 0xffef30;
    private int textShadowColor = 0x80000000;

    private long DURATION_FIX_TOP_STYLE = 8000;//置顶弹幕停留时间

    private int textSize = 18;

    @Override
    public Danmakus parse() {
        if (mDataSource != null) {
            SSKDanmakuSource source = (SSKDanmakuSource) mDataSource;
            JSONArray jArray = source.data();
            if (jArray != null) {
                Danmakus result = new Danmakus(ST_BY_TIME, false, mContext.getBaseComparator());
                BaseDanmaku item;
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jItem = jArray.optJSONObject(i);
                    long time = jItem.optLong("position");// 出现时间
                    String content = jItem.optString("content");// 弹幕内容
                    //long groupCount = jItem.optInt("group_count");//
                    //long id = jItem.optLong("id");// 弹幕ID
                    String direction = jItem.optString("direction");// 弹幕样式
                    if (direction.equalsIgnoreCase("HEADLINE")) {
                        item = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_FIX_TOP, mContext);
                        if (item != null) {
                            item.duration = new Duration(DURATION_FIX_TOP_STYLE);
                        }
                    } else {
                        item = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, mContext);
                    }
                    if (item != null) {
                        item.setTime(time);
                        item.text = content;
                        item.textSize = textSize * (mDispDensity - 0.6f);
                        item.textColor = textColor;
                        item.textShadowColor = textShadowColor;
                        item.setTimer(mTimer);
                        item.flags = mContext.mGlobalFlagValues;
                        Object lock = result.obtainSynchronizer();
                        synchronized (lock) {
                            result.addItem(item);
                        }
                    }
                }
                return result;
            }
        }

        return null;
    }

    public BaseDanmaku parseItem(DanmakaSendEvent danmakaSend) {
        if (mContext == null) {
            Timber.e(new Throwable("BaseDanmaku parseItem error"));
            return null;
        }
        BaseDanmaku item = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, mContext);
        if (item != null) {
            item.setTime(danmakaSend.getPosition());
            item.text = danmakaSend.getContent();
            item.textSize = textSize * (mDispDensity - 0.6f);
            item.textColor = sendBarrageTextColor;
            item.textShadowColor = textShadowColor;
            item.setTimer(mTimer);
            item.flags = mContext.mGlobalFlagValues;
        }
        return item;
    }

}
