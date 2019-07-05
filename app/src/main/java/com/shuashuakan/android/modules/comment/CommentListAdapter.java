package com.shuashuakan.android.modules.comment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.shuashuakan.android.R;
import com.shuashuakan.android.constant.Constants;
import com.shuashuakan.android.data.api.model.comment.ApiComment;
import com.shuashuakan.android.data.api.model.comment.ApiMedia;
import com.shuashuakan.android.data.api.model.comment.ApiMediaInfo;
import com.shuashuakan.android.data.api.model.comment.CommentListResp;
import com.shuashuakan.android.modules.account.AccountManager;
import com.shuashuakan.android.modules.widget.StrickRoundSpan;
import com.shuashuakan.android.spider.Spider;
import com.shuashuakan.android.spider.SpiderEventNames;
import com.shuashuakan.android.utils.Contexts;
import com.shuashuakan.android.utils.StringUtils;
import com.shuashuakan.android.utils.TimeUtil;
import com.shuashuakan.android.utils.UtilsKt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.shuashuakan.android.modules.timeline.profile.ProfileTimelineAdapterKt.setImageExpectMeasure;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/10
 * Description:
 */
public class CommentListAdapter extends RecyclerArrayAdapter<ApiComment> {

    private RecyclerView recyclerView;
    public static final String TYPE_LONG = "LONG_IMAGE";
    public static final String TYPE_GIF = "ANIMATION";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_VIDEO = "VIDEO";
    private Context context;
    private AccountManager accountManager;

    public CommentListAdapter(Context context, RecyclerView recyclerView, AccountManager accountManager) {
        super(context);
        this.context = context;
        this.recyclerView = recyclerView;
        this.accountManager = accountManager;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommentVideoListVH(parent);
    }

    private OnCommentListOperationListener operationListener;

    public void setOperationListener(OnCommentListOperationListener operationListener) {
        this.operationListener = operationListener;
    }

    public class CommentVideoListVH extends BaseViewHolder<ApiComment> {
        private SimpleDraweeView userAvatar;

        private ImageView praiseIv, commentVideo, btnPlay;

        private TextView userName, content, commentTime, praiseTv, commentReply, commentShare, imageTag;

        private FrameLayout videoLayout;

        private RecyclerView replyRecycler;

        private LinearLayout praiseLl, contentLayout;

        private ReplyListAdapter replyListAdapter;

        private LoadMoreReplyFooter loadMoreReplyFooter;

        public CommentVideoListVH(ViewGroup parent) {
            super(parent, R.layout.item_comment_video);
            userAvatar = $(R.id.user_avatar);
            praiseIv = $(R.id.praise_iv);
            userName = $(R.id.user_name);
            content = $(R.id.home_container);
            commentTime = $(R.id.comment_time);
            commentReply = $(R.id.comment_reply);
            commentShare = $(R.id.comment_share);
            commentVideo = $(R.id.comment_video);
            videoLayout = $(R.id.comment_video_layout);
            praiseTv = $(R.id.praise_tv);
            replyRecycler = $(R.id.reply_recycler);
            praiseLl = $(R.id.praise_ll);
            btnPlay = $(R.id.btn_comment_play);
            imageTag = $(R.id.comment_image_tag);
            contentLayout = $(R.id.content_layout);
            replyRecycler.setLayoutManager(new LinearLayoutManager(CommentListAdapter.this.getContext()));

            replyRecycler.setAdapter(replyListAdapter = new ReplyListAdapter(CommentListAdapter.this.getContext(), accountManager));

            loadMoreReplyFooter = new LoadMoreReplyFooter(CommentListAdapter.this.getContext());

            replyListAdapter.addFooter(loadMoreReplyFooter);

            replyListAdapter.setOnReplyOperationListener((data, position, listener) -> {
                if (operationListener != null && !isDeleted(data) && data.getId() != null) {
                    operationListener.onPraise(data, getDataPosition(), true, position, listener);
                }
            });
            replyListAdapter.setContentOperationListener((data, position) -> {
                if (operationListener != null)
                    operationListener.onReplyClick(replyListAdapter.getItem(position), getDataPosition(), false);
            });
        }

        @SuppressLint("CheckResult")
        @Override
        public void setData(ApiComment data) {
            super.setData(data);
            if (!StringUtils.isEmpty(data.getAuthor().getAvatar())) {
                userAvatar.setImageURI(data.getAuthor().getAvatar());
            } else {
                userAvatar.setImageURI(data.getAuthor().getDefaultAvatar());
            }

            if (accountManager.hasAccount()) {
                if (accountManager.account().getUserId() == data.getAuthor().getUserId()) {
                    commentReply.setVisibility(View.GONE);
                } else {
                    commentReply.setVisibility(View.VISIBLE);
                }
            } else {
                commentReply.setVisibility(View.VISIBLE);
            }

            RxView.clicks(userAvatar)
                    .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        if (operationListener != null && !isDeleted(data) && data.getId() != null)
                            operationListener.onUserAvatarClick(data);
                    });
            userName.setText(data.getAuthor().getNickName() + " ");

            if (data.getAuthor().getLabels() != null && data.getAuthor().getLabels().size() > 0) {
                SpannableString nameFlag = new SpannableString(data.getAuthor().getLabels().get(0).getContent());
                if (!TextUtils.isEmpty(nameFlag)) {
                    nameFlag.setSpan(new StrickRoundSpan(context, ContextCompat.getColor(getContext(), R.color.comment_send_color),
                                    ContextCompat.getColor(getContext(), R.color.comment_send_color)),
                            0, nameFlag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    userName.append(nameFlag);
                }
            }
            content.setText(data.getContent());
            praiseTv.setText("" + data.getLikeCount());

            if (data.getContent().isEmpty()) {
                content.setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
            }

            commentTime.setText(TimeUtil.getTimeFormatText(new Date(data.getCreateAt())));

            isPraise(data.getLiked(), praiseIv);

            replyListAdapter.setUserId(data.getAuthor().getUserId());

            if (data.getNewestComments() != null
                    && data.getNewestComments().getComments() != null
                    && !data.getNewestComments().getComments().isEmpty()) {
                replyListAdapter.clear();
                replyListAdapter.addAll(data.getNewestComments().getComments());
            } else {
                replyListAdapter.clear();
            }
            updateReplyFooterStatus(getDataPosition(), loadMoreReplyFooter);

            UtilsKt.noDoubleClick(praiseLl, () -> {
                if (operationListener != null && !isDeleted(data) && data.getId() != null) {
                    operationListener.onPraise(data, getDataPosition(), false, -1, new ReplyListAdapter.OnListener() {
                        @Override
                        public void onCancelPraise() {
                            data.setLiked(false);
                            data.setLikeCount(data.getLikeCount() - 1);
                            isPraise(data.getLiked(), praiseIv);
                            praiseTv.setText("" + data.getLikeCount());
                        }

                        @Override
                        public void onPraise() {
                            data.setLiked(true);
                            data.setLikeCount(data.getLikeCount() + 1);
                            isPraise(data.getLiked(), praiseIv);
                            praiseTv.setText("" + data.getLikeCount());
                        }
                    });
                }
                return null;
            });
            UtilsKt.noDoubleClick(commentReply, () -> {
                if (operationListener != null && !isDeleted(data) && data.getId() != null) {
                    operationListener.onReplyClick(data, getDataPosition(), true);
                }
                return null;
            });

            UtilsKt.noDoubleClick(commentShare, () -> {
                if (operationListener != null && !isDeleted(data) && data.getId() != null) {
                    operationListener.onShareClick(data.getId(), data.getMedia());
                }
                return null;
            });
            loadMoreReplyFooter.getLoadMoreTv().setOnClickListener(v -> operationListener.onLoadReply(getItem(getDataPosition())));

            loadMoreReplyFooter.getPackUpTv().setOnClickListener(v -> {
                ApiComment apiComment = Objects.requireNonNull(getItem(getDataPosition()).getNewestComments()).getComments().get(0);
                Objects.requireNonNull(getItem(getDataPosition()).getNewestComments()).getComments().clear();
                Objects.requireNonNull(getItem(getDataPosition()).getNewestComments()).getComments().add(apiComment);

                Objects.requireNonNull(getItem(getDataPosition()).getNewestComments()).setHasMore(true);
                Objects.requireNonNull(getItem(getDataPosition()).getNewestComments()).setNextCursor(null);

                replyListAdapter.clear();
                replyListAdapter.add(apiComment);

                recyclerView.scrollToPosition(getDataPosition());

                updateReplyFooterStatus(getDataPosition(), loadMoreReplyFooter);
            });

            replyListAdapter.setOnItemClickListener(position -> {
                if (operationListener != null)
                    operationListener.onReplyClick(replyListAdapter.getItem(position), getDataPosition(), false);
            });
            List<ApiMedia> media = data.getMedia();
            if (media == null) {
                videoLayout.setVisibility(View.GONE);
            } else {
                videoLayout.setVisibility(View.VISIBLE);
                String thumbUrl = media.get(0).getThumbUrl();
                String mediaType = media.get(0).getMediaType();
                int width = 0, height = 0;
                if (media.size() > 0) {
                    if (mediaType.equals(TYPE_VIDEO)) {
                        imageTag.setVisibility(View.GONE);
                        btnPlay.setVisibility(View.VISIBLE);
//            adjustImageMeasure(commentVideo, media.get(0).getThumbWidth(), media.get(0).getThumbHeight(), contentLayout.getMeasuredWidth());
                    } else {
                        imageTag.setVisibility(View.VISIBLE);
                        btnPlay.setVisibility(View.GONE);
                        setImageExpectMeasure(commentVideo, media.get(0).getThumbWidth(), media.get(0).getThumbHeight(),
                                Contexts.dip(getContext(), 120));
                        switch (mediaType) {
                            case TYPE_IMAGE:
                                imageTag.setVisibility(View.GONE);
                                UtilsKt.setImageForGlide(context, thumbUrl, commentVideo, true);
                                break;
                            case TYPE_LONG:
                                imageTag.setText(context.getResources().getString(R.string.string_long_picture));
                                UtilsKt.setImageForGlide(context, thumbUrl, commentVideo, true);
                                break;
                            case TYPE_GIF:
                                imageTag.setText("GIF");
                                UtilsKt.setImageForGlide(context, thumbUrl, commentVideo, false);
                                break;
                        }
                    }
//          UtilsKt.setImageForGlide(context, UtilsKt.imageUrl2WebP(thumbUrl, width, height), commentVideo, true);

                    UtilsKt.noDoubleClick(commentVideo, () -> {
                        String originalUrl = null, waterUrl = null;
                        int mediaWidth = 0, mediaHeight = 0;
                        if (!isDeleted(data) && data.getId() != null) {
                            List<ApiMediaInfo> mediaInfo = media.get(0).getMediaInfo();
                            if (mediaInfo != null) {
                                for (int i = 0; i < mediaInfo.size(); i++) {
                                    String clarityType = mediaInfo.get(i).getClarityType();
                                    if (clarityType.equals("ORIGINAL")) {
                                        originalUrl = mediaInfo.get(i).getUrl();
                                        mediaWidth = mediaInfo.get(i).getWidth();
                                        mediaHeight = mediaInfo.get(i).getHeight();
                                    } else if (clarityType.equals("WATERMARK")) {
                                        waterUrl = mediaInfo.get(i).getUrl();
                                    }
                                }
                            }
                            switch (mediaType) {
                                case TYPE_GIF:
                                    CommentImageShowActivity.Companion.create(context, thumbUrl, mediaType, waterUrl, data.getTargetId());
                                    break;
                                default:
                                    CommentImageShowActivity.Companion.create(context, thumbUrl, commentVideo, mediaType, waterUrl, data.getTargetId());
                                    break;
                            }
                        }
                        return null;
                    });
                }
            }
        }

        public void deleteReply(ApiComment data) {
            getItem(getDataPosition()).getNewestComments().getComments().remove(data);
            replyListAdapter.remove(data);
            updateReplyFooterStatus(getDataPosition(), loadMoreReplyFooter);
        }

        public void addReply(ApiComment data) {
            if (getItem(getDataPosition()).getNewestComments() == null) {
                CommentListResp.CommentResult commentResult = new CommentListResp.CommentResult(false, null, null, new ArrayList<>(), new ArrayList<>());
                getItem(getDataPosition()).setNewestComments(commentResult);
            }
            getItem(getDataPosition()).getNewestComments().getComments().add(0, data);
            replyListAdapter.insert(data, 0);
            updateReplyFooterStatus(getDataPosition(), loadMoreReplyFooter);
        }


        public void addAllReply(List<ApiComment> comments, CommentListResp.CommentCursor commentCursor) {
            if (commentCursor == null) {
                replyListAdapter.clear();
            }
            replyListAdapter.addAll(comments);
            updateReplyFooterStatus(getDataPosition(), loadMoreReplyFooter);
        }
    }


    private void adjustImageMeasure(ImageView commentVideo, int width, int height, int maxWidth) {

        if (maxWidth == 0) {
            maxWidth = Contexts.getScreenSize(getContext()).x - Contexts.dip(getContext(), 110);
        }
        double scaleImg = (double) width / height;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) commentVideo.getLayoutParams();
        int dipHeight = Contexts.dip(getContext(), 140);
        if (maxWidth > (int) (dipHeight * 1.778f))
            maxWidth = (int) (dipHeight * 1.778f);
        if (width < dipHeight && height < dipHeight) {
            params.width = width;
            params.height = height;
            commentVideo.setLayoutParams(params);
        } else {
            if (scaleImg < 1.778f) {
                int realWidth = Double.valueOf(dipHeight * scaleImg).intValue();
                params.width = realWidth > maxWidth ? maxWidth : realWidth;
                params.height = dipHeight;
                commentVideo.setLayoutParams(params);
            } else {
                int realWidth = (int) (dipHeight * 1.778f);
                params.width = realWidth > maxWidth ? maxWidth : realWidth;
                params.height = dipHeight;
                commentVideo.setLayoutParams(params);
            }
        }
        if (width == 0 && height == 0) {
            params.width = dipHeight;
            params.height = dipHeight;
            commentVideo.setLayoutParams(params);
        }
    }

    private void isPraise(boolean b, ImageView praiseIv) {
        praiseIv.setImageResource(b ? R.drawable.ic_comment_liked : R.drawable.ic_comment_like);
    }

    /**
     * 更新 子评论 尾布局
     */
    private void updateReplyFooterStatus(int dataPosition, LoadMoreReplyFooter loadMoreReplyFooter) {
        if (getItem(dataPosition).getNewestComments() != null) {
            if (getItem(dataPosition).getNewestComments().getHasMore()) {
                loadMoreReplyFooter.getLoadMoreTv().setVisibility(View.VISIBLE);
            } else {
                loadMoreReplyFooter.getLoadMoreTv().setVisibility(View.GONE);
            }
            if (getItem(dataPosition).getNewestComments().getComments().size() > 1) {
                loadMoreReplyFooter.getPackUpTv().setVisibility(View.VISIBLE);
            } else {
                loadMoreReplyFooter.getPackUpTv().setVisibility(View.GONE);
            }
        } else {
            loadMoreReplyFooter.getPackUpTv().setVisibility(View.GONE);
            loadMoreReplyFooter.getLoadMoreTv().setVisibility(View.GONE);
        }
    }

    public boolean isDeleted(ApiComment data) {
        if (data != null && data.getState() != null) {
            return data.getState().equals("REMOVED");
        }
        return false;
    }


    public interface OnCommentListOperationListener {
        void onPraise(ApiComment data, int position, boolean isSonPraise, int sonPosition, ReplyListAdapter.OnListener onListener);

        void onLoadReply(ApiComment data);

        void onReplyClick(ApiComment data, int position, boolean isBtn);

        void onUserAvatarClick(ApiComment data);

        void onShareClick(Long id, @Nullable List<ApiMedia> media);
    }
}