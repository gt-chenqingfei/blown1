package com.shuashuakan.android.modules.comment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.shuashuakan.android.R;
import com.shuashuakan.android.constant.Constants;
import com.shuashuakan.android.utils.KeyBoardUtil;
import com.shuashuakan.android.utils.SoftKeyBoardListener;
import com.shuashuakan.android.utils.StringUtils;
import com.shuashuakan.android.utils.Strings;
import com.shuashuakan.android.utils.UtilsKt;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/13
 * Description:
 */
public class AddCommentPopup {
    private PopupWindow popupWindow;
    private Activity mContext;
    private View background;
    private EditText editText;
    private SimpleDraweeView selImg;
    private ImageView addImg;
    private FrameLayout selLayout;
    private LinearLayout emojiLayout;
    private TextView sendComment;

    private OnCommentListener onCommentListener;

    private String hint;
    private int maxLen = 140;
    private boolean isShowAdd;
    private String mediaPath = null;
    private int mediaWidth = 0, mediaHeight = 0;
    private String pictureType = null;
    private String type = null;
    private int emojiList[] = {0x1F602, 0x1F61A, 0x1F64C, 0x1F525, 0x26FD, 0x1F60D, 0x1F630, 0x1F621};
    private boolean lastIsReply = false;
    private long replyCommentId = -1;
    private int replyPosition = -1;
    private String replyAuthorName = null;
    private SoftKeyBoardListener mSoftKeyBoardListener;

    public AddCommentPopup(Activity mContext, String hint, Boolean isShowAdd, OnCommentListener commentListener) {
        this.mContext = mContext;
        this.hint = hint;
        this.isShowAdd = isShowAdd;
        this.onCommentListener = commentListener;
        createPopWindow();
    }

    @SuppressLint("CheckResult")
    private void createPopWindow() {
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(false);
        //防止虚拟软键盘被弹出菜单遮住
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        popupWindow.setFocusable(true);
        ColorDrawable drawable = new ColorDrawable();
        popupWindow.setBackgroundDrawable(drawable);

        View rootView = View.inflate(mContext, R.layout.dialog_add_comment_fragment, null);
        background = rootView.findViewById(R.id.background);
        editText = rootView.findViewById(R.id.add_comment_et);
        addImg = rootView.findViewById(R.id.add_comment_iv);
        selLayout = rootView.findViewById(R.id.sel_media_l);
        selImg = rootView.findViewById(R.id.sel_media_iv);
        ImageView delImg = rootView.findViewById(R.id.del_media_iv);
        emojiLayout = rootView.findViewById(R.id.comment_emoji_layout);
        sendComment = rootView.findViewById(R.id.send_comment_tv);
        popupWindow.setContentView(rootView);
        initEmojiLayout();

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCommentListener == null) {
                    return;
                }
                String s = editText.getText().toString().trim();
                if (!StringUtils.isEmpty(s) || !StringUtils.isEmpty(mediaPath)) {
                    if (lastIsReply) {
                        onCommentListener.onReply(replyCommentId, s, replyPosition);
                    } else {
                        onCommentListener.onComment(s, mediaPath);
                    }
                    dismiss();
                    sendComment.setVisibility(View.GONE);
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.string_empty_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isShowAdd) {
            addImg.setVisibility(View.VISIBLE);
            addImg.setOnClickListener(v -> {
//        showAlbum();
                dismiss();
                if (onCommentListener != null)
                    onCommentListener.onShowAlbum();
            });

            delImg.setOnClickListener(v -> {
                clearMedia();
                if (editText.getText().toString().length() == 0)
                    sendComment.setVisibility(View.GONE);
            });
        }
        setListeners();
    }

    private void initEmojiLayout() {
        for (int i = 0; i < 8; i++) {
            TextView textView = new TextView(mContext);
            textView.setText(new String(Character.toChars(emojiList[i])));
            textView.setTextColor(mContext.getResources().getColor(R.color.comment_content_color));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
            textView.setOnClickListener(v -> {
                addEmoji(textView.getText().toString());
            });
            emojiLayout.addView(textView);
        }
    }

    private void setListeners() {
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (mSoftKeyBoardListener == null) {
            mSoftKeyBoardListener = new SoftKeyBoardListener(mContext);
        }

        mSoftKeyBoardListener.setOnSoftKeyBoardChangeListener(new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {

            }

            @Override
            public void keyBoardHide(int height) {
                popupWindow.dismiss();
            }
        });


        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Editable editable = editText.getText();
                int len = Strings.getTextLength(editText.getText().toString());
                if (len > maxLen) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.string_beyound_length), Toast.LENGTH_SHORT).show();
                    int selEndIndex = Selection.getSelectionEnd(editable);
                    String str = editable.toString();
                    //截取新字符串
                    String newStr = str.substring(0, maxLen);
                    editText.setText(newStr);
                    editable = editText.getText();

                    //新字符串的长度
                    int newLen = editable.length();
                    //旧光标位置超过字符串长度
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length();
                    }
                    //设置新光标所在的位置
                    Selection.setSelection(editable, selEndIndex);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0)
                    sendComment.setVisibility(View.VISIBLE);
                if (editable.length() == 0 && selLayout.getVisibility() == View.GONE)
                    sendComment.setVisibility(View.GONE);
            }
        });
        popupWindow.setOnDismissListener(() -> {
            lastIsReply = false;
            if (onCommentListener != null)
                onCommentListener.onRefreshTextView(editText.getText().toString(), mediaPath, replyAuthorName);
        });
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.clearFocus();
        KeyBoardUtil.hideInputSoftFromWindowMethod(mContext, editText);
        popupWindow.dismiss();

    }

    public void destroy() {
        mSoftKeyBoardListener.setOnSoftKeyBoardChangeListener(null);
        mContext = null;
        onCommentListener = null;
        mSoftKeyBoardListener = null;
    }


    private void addComment(View clickView) {
        popupWindow.showAtLocation(clickView, 0, 0, 0);

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        KeyBoardUtil.showInputSoftFromWindowMethod(mContext);
    }

    public void comment(View clickView) {
        addImg.setVisibility(View.VISIBLE);
        if (lastIsReply) {
//      clearText();
            lastIsReply = false;
        }
        replyPosition = -1;
        replyCommentId = -1;
        replyAuthorName = null;
        editText.setHint(hint);
//    editText.setOnEditorActionListener((v, actionId, event) -> {
//      if (actionId == EditorInfo.IME_ACTION_SEND) {
//        String s = editText.getText().toString().trim();
//        if (!StringUtil.isEmpty(s) || !StringUtil.isEmpty(mediaPath)) {
//          onCommentListener.onComment(s, mediaPath);
//          dismiss();
//          sendComment.setVisibility(View.GONE);
//        } else {
//          Toast.makeText(mContext, "文本不能为空", Toast.LENGTH_SHORT).show();
//        }
//      }
//      return false;
//    });
        addComment(clickView);
    }

    public void showSelImage(String path, int width, int height, String pictureType, String type) {
        mediaPath = path;
        mediaWidth = width;
        mediaHeight = height;
        this.pictureType = pictureType;
        this.type = type;
        selLayout.setVisibility(View.VISIBLE);
        UtilsKt.setImagePath(selImg, mContext, mediaPath, 4);
        sendComment.setVisibility(View.VISIBLE);

        if (onCommentListener != null)
            onCommentListener.onRefreshTextView(editText.getText().toString(), mediaPath, replyAuthorName);
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public int getMediaWidth() {
        return mediaWidth;
    }

    public int getMediaHeight() {
        return mediaHeight;
    }

    public String getPictureType() {
        return pictureType;
    }

    public void setMediaWidth(int mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public void setMediaHeight(int mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public String getType() {
        return type;
    }

    public void addEmoji(String emoji) {
        editText.append(emoji);
    }

    public void clearText() {
        editText.setText("");
    }

    public boolean isLastIsReply() {
        return lastIsReply;
    }

    public long getReplyCommentId() {
        return replyCommentId;
    }

    public int getReplyPosition() {
        return replyPosition;
    }

    public String getReplyAuthorName() {
        return replyAuthorName;
    }

    public void clearMedia() {
        selLayout.setVisibility(View.GONE);
        mediaPath = null;
        mediaWidth = 0;
        mediaHeight = 0;
        pictureType = null;
        type = null;
    }

    public void reply(View clickView, Long commentId, String authorNickName, int position) {
        //点击回复 恢复二级输入框
        clearMedia();
//    sendComment.setVisibility(View.GONE);
        addImg.setVisibility(View.GONE);
//    clearText();
        lastIsReply = true;
        replyCommentId = commentId;
        replyAuthorName = authorNickName;
        replyPosition = position;
        editText.setHint("@" + authorNickName);
//    editText.setOnEditorActionListener((v, actionId, event) -> {
//      if (actionId == EditorInfo.IME_ACTION_SEND) {
//        String s = editText.getText().toString().trim();
//        if (!StringUtil.isEmpty(s)) {
//          onCommentListener.onReply(commentId, s, position);
//          dismiss();
//        } else {
//          Toast.makeText(mContext, "文本不能为空", Toast.LENGTH_SHORT).show();
//        }
//      }
//      return false;
//    });
        addComment(clickView);
    }

    public interface OnCommentListener {
        /**
         * 评论完成
         */
        void onComment(@Nullable String content, @Nullable String mediaPath);

        void onReply(Long commentId, String content, int position);

        void onShowAlbum();

        void onRefreshTextView(@Nullable String text, @Nullable String media_path, @Nullable String replyAuthorName);
    }
}
