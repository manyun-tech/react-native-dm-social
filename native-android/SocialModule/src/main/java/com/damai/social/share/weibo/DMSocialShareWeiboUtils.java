package com.damai.social.share.weibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.damai.social.share.DMSocialShare;
import com.damai.social.R;
import com.damai.social.share.bean.ShareContent;
import com.damai.social.share.bean.SocialConstants;
import com.damai.social.share.interf.IShareListener;
import com.damai.social.share.util.ImageUtil;
import com.damai.social.share.util.ToastUtil;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;

import java.io.InputStream;

public class DMSocialShareWeiboUtils implements IWeiboHandler.Response {
    public static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    /**
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     * <br>
     * <br>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * <br>
     */
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    private Context mContext;
    private IWeiboShareAPI mIWeiboShareAPI;
    private IShareListener mIShareListener;
    private volatile static DMSocialShareWeiboUtils instance;

    public static DMSocialShareWeiboUtils getInstance() {
        if (instance == null) {
            synchronized (DMSocialShare.class) {
                if (instance == null) {
                    instance = new DMSocialShareWeiboUtils();
                }
            }
        }
        return instance;
    }

    public boolean init(final Context context) {
        mContext = context;

        String appkey = DMSocialShare.getInstance().getConfig().sinaAppKey;
        if (!TextUtils.isEmpty(appkey)) {
            mIWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, appkey);
            if (checkSinaVersin(context)) {
                mIWeiboShareAPI.registerApp();
                mIWeiboShareAPI.handleWeiboResponse(((Activity) context).getIntent(), this);
                return true;
            } else {
                return false;
            }
        } else {
            ToastUtil.toast(mContext, "sina app key is null");
            return false;
        }
    }

    /**
     * 新浪微博分享方法
     *
     * @param shareContentBean 分享的内容
     */
    public void shareToWeibo(Context context, ShareContent shareContentBean, IShareListener iShareListener) {
        if (mIWeiboShareAPI == null) return;
        mIShareListener = iShareListener;
        switch (shareContentBean.getShareWay()) {
            case SocialConstants.SHARE_TYPE_WAY_TEXT:
                shareText(shareContentBean);
                break;
            case SocialConstants.SHARE_TYPE_WAY_IMAGE:
                sharePicture(context, shareContentBean);
                break;
            case SocialConstants.SHARE_TYPE_WAY_WEB:
                shareWebPage(context, shareContentBean);
                break;
        }
    }

    /*
     * 分享文字
     */
    private void shareText(ShareContent shareContentBean) {
        //初始化微博的分享消息
        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.mediaObject = getTextObj(shareContentBean.getContent());
        //初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = buildTransaction("sinatext");
        request.message = weiboMessage;
        //发送请求信息到微博，唤起微博分享界面
        mIWeiboShareAPI.sendRequest((Activity) mContext, request);
    }

    /*
     * 分享图片
     */
    private void sharePicture(Context context, ShareContent shareContentBean) {
        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.mediaObject = getImageObj(context, shareContentBean.getImageResource());
        //初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = buildTransaction("sinaimage");
        request.message = weiboMessage;
        //发送请求信息到微博，唤起微博分享界面
        mIWeiboShareAPI.sendRequest((Activity) mContext, request);
    }

    private void shareWebPage(final Context context, final ShareContent shareContentBean) {
        final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        final WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        String title = shareContentBean.getTitle();
        String content = shareContentBean.getContent();
        mediaObject.title = title;
        mediaObject.description = content;
        mediaObject.actionUrl = shareContentBean.getTargetUrl();
        mediaObject.defaultText = content;

        if (!TextUtils.isEmpty(content))
            weiboMessage.textObject = getTextObj(content);


        //DialogHelper.showWaitDialog(context, "分享中...");
        //mediaObject.setThumbImage(shareContentBean.getBitmap());
        weiboMessage.mediaObject = mediaObject;
        String imageUrl = shareContentBean.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageUtil.getHtmlByteArray(shareContentBean.getImageUrl(), new ImageUtil.OnloadImageListener() {
                @Override
                public void loadSuccess(InputStream inStream) {
                    //DialogHelper.hideWaitDialog();
                    //  设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
                    Bitmap bmp = ImageUtil.Bytes2Bimap(ImageUtil.inputStreamToByte(inStream));
                    mediaObject.setThumbImage(bmp);
                    weiboMessage.mediaObject = mediaObject;
                    ImageObject imageObject = new ImageObject();
                    imageObject.setImageObject(bmp);
                    weiboMessage.imageObject = imageObject;
                    startShare(weiboMessage);
                }

                @Override
                public void loadFailed() {
                    ToastUtil.toast(mContext, R.string.share_failed);
                    //DialogHelper.hideWaitDialog();
                }
            });
        } else {
            mediaObject.thumbData = ImageUtil.bmpToByteArray(shareContentBean.getBitmap(),true);
            weiboMessage.mediaObject = mediaObject;
            //ImageObject imageObject = new ImageObject();
            //imageObject.setImageObject(shareContentBean.getBitmap());
            //weiboMessage.imageObject = imageObject;
            startShare(weiboMessage);
        }

    }

    /**
     //     * 读取应用的Bitmap，意义在于设置一个不得超过 32kb小图，方便分享大图
     //     */
//    private Bitmap getAppIconBitmap(Context context) {
//        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//    }

    /**
     * 分享的方式是混合模式 web和image结合
     */
    private void startShare(WeiboMultiMessage weiboMessage) {
        //初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = buildTransaction("sinaweb");
        request.multiMessage = weiboMessage;
        //发送请求信息到微博，唤起微博分享界面
        mIWeiboShareAPI.sendRequest((Activity) mContext, request);
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj(String text) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }

    private ImageObject getImageObj(Context context, int picResource) {
        ImageObject imageObject = new ImageObject();
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), picResource);
        imageObject.setImageObject(bmp);
        return imageObject;
    }


    private boolean checkSinaVersin(final Context context) {
        // 获取微博客户端相关信息，如是否安装、支持 SDK 的版本
        boolean isInstalledWeibo = mIWeiboShareAPI.isWeiboAppInstalled();
        //int supportApiLevel = sinaAPI.getWeiboAppSupportAPI();

        // 如果未安装微博客户端，设置下载微博对应的回调
        if (isInstalledWeibo) {
            return true;
        } else {
            if (context != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.toast(context, R.string.install_weibo_tips);
                    }
                });
            }
            return false;
        }
    }

    public void response(Intent intent) {
        if (intent != null && mIWeiboShareAPI != null) {
            mIWeiboShareAPI.handleWeiboResponse(intent, this);
        }
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                if (mIShareListener != null)
                    mIShareListener.onComplete();
                //EventBus.getDefault().post(new BaseEvent.ShareSuccessEvent());
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                if (mIShareListener != null)
                    mIShareListener.onCancel();
                //EventBus.getDefault().post(new BaseEvent.ShareCancelEvent());
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                if (mIShareListener != null)
                    mIShareListener.onError();
                //EventBus.getDefault().post(new BaseEvent.ShareFailedEvent());
                break;
        }
    }
}
