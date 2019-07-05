package com.shuashuakan.android.event;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/07/27
 * Description:
 */
public class WeChatBindEvent {
    public String state;
    public String code;

    public WeChatBindEvent(String state, String code) {
        this.state = state;
        this.code = code;
    }
}
