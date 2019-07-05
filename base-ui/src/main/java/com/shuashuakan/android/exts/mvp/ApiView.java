package com.shuashuakan.android.exts.mvp;

import android.support.annotation.NonNull;

/**
 * @author twocity
 */
public interface ApiView extends MvpView {
    /**
     * z
     * 显示消息，可以是Toast也可以是SnackBar
     * 这个方法的应用场景是用来显示某些错误的信息，比如API出错；对用户的操作做出反馈，比如删除订单成功。
     * View层应根据具体需要来判断用何种方式来显示，一般来说就是用Toast
     *
     * @param message 需要展示的消息
     */
    void showMessage(@NonNull String message);
}
