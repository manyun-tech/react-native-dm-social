package com.damai.social.share.wechat;

import android.content.Context;
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
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.InputStream;

public class DMSocialShareWeChatUtils {

    /**
     * 会话
     */
    public static final int WEIXIN_SHARE_TYPE_TALK = SendMessageToWX.Req.WXSceneSession;

    /**
     * 朋友圈
     */
    public static final int WEIXIN_SHARE_TYPE_TIMELINE = SendMessageToWX.Req.WXSceneTimeline;

    /**
     * 收藏
     */
    public static final int WEIXIN_SHARE_TYPE_FAVORITE = SendMessageToWX.Req.WXSceneFavorite;

    private Context mContext;
    private IWXAPI iwxapi;
    private static IShareListener mIShareListener;
    private volatile static DMSocialShareWeChatUtils instance;

    public static DMSocialShareWeChatUtils getInstance() {
        if (instance == null) {
            synchronized (DMSocialShare.class) {
                if (instance == null) {
                    instance = new DMSocialShareWeChatUtils();
                }
            }
        }
        return instance;
    }

    public boolean init(Context context) {
        mContext = context;
        iwxapi = WXAPIFactory.createWXAPI(mContext, DMSocialShare.getInstance().getConfig().wechatAppId);
        if (!iwxapi.isWXAppInstalled()) {
            ToastUtil.toast(context, R.string.install_wechat_tips);
            return false;
        }
        iwxapi.registerApp(DMSocialShare.getInstance().getConfig().wechatAppId);
        return true;
    }

    public void shareToWeChat(ShareContent shareContent, int shareType, IShareListener iShareListener) {
        mIShareListener = iShareListener;
        switch (shareContent.getShareWay()) {
            case SocialConstants.SHARE_TYPE_WAY_TEXT:
                shareText(shareType, shareContent);
                break;
            case SocialConstants.SHARE_TYPE_WAY_IMAGE:
                sharePicture(shareType, shareContent);
                break;
            case SocialConstants.SHARE_TYPE_WAY_WEB:
                shareWebPage(shareType, shareContent);
                break;
        }
    }

    public static IShareListener getIShareListener() {
        return mIShareListener;
    }

    private void shareText(int shareType, ShareContent shareContentBean) {

    }

    private void sharePicture(int shareType, ShareContent shareContentBean) {

    }

    private void shareWebPage(final int shareType, final ShareContent shareContentBean) {
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareContentBean.getTargetUrl();
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareContentBean.getTitle();
        msg.description = shareContentBean.getContent();

        final SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.scene = shareType;

        String imageUrl = shareContentBean.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageUtil.getHtmlByteArray(imageUrl, new ImageUtil.OnloadImageListener() {
                @Override
                public void loadSuccess(InputStream inStream) {
                    msg.thumbData = ImageUtil.inputStreamToByte(inStream);
                    req.message = msg;
                    iwxapi.sendReq(req);
                }

                @Override
                public void loadFailed() {
                    ToastUtil.toast(mContext, R.string.share_failed);
                }
            });
        } else {
            if (shareContentBean.getBitmap() != null) {
                msg.thumbData = ImageUtil.bmpToByteArray(shareContentBean.getBitmap(), true);
                //msg.setThumbImage(shareContentBean.getBitmap());  png图片背景会变黑
                req.message = msg;
                iwxapi.sendReq(req);
            } else {
                ToastUtil.toast(mContext, R.string.share_failed);
            }
        }

//            }
//        }).start();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
