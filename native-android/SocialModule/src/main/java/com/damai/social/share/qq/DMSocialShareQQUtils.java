package com.damai.social.share.qq;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.damai.social.share.DMSocialShare;
import com.damai.social.R;
import com.damai.social.share.bean.ShareContent;
import com.damai.social.share.interf.IShareListener;
import com.damai.social.share.util.ImageUtil;
import com.damai.social.share.util.ToastUtil;
import com.tencent.connect.share.QQShare;
import com.tencent.open.utils.ThreadManager;
import com.tencent.tauth.Tencent;


public class DMSocialShareQQUtils {
    private Tencent mTencent;
    private Context mContext;

    private volatile static DMSocialShareQQUtils instance;

    public static DMSocialShareQQUtils getInstance() {
        if (instance == null) {
            synchronized (DMSocialShare.class) {
                if (instance == null) {
                    instance = new DMSocialShareQQUtils();
                }
            }
        }
        return instance;
    }

    public boolean init(Context context) {
        if (QQLoginManager.isInstallQQ(context)){
            mContext = context;
            mTencent = Tencent.createInstance(DMSocialShare.getInstance().getConfig().qqAppId, context);
            return true;
        } else {
            ToastUtil.toast(context, R.string.install_qq_tips);
            return false;
        }
    }

    public void shareToQQ(ShareContent shareContentBean, boolean isShareToQZone, IShareListener iShareListener) {
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareContentBean.getTitle());  // 标题，必填
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareContentBean.getTargetUrl()); // 跳转的页面 必填（好友点击消息后的链接）
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareContentBean.getContent());

        String imageUrl = shareContentBean.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        } else {
            // TODO:  子线程处理
            Bitmap bitmap = shareContentBean.getBitmap();
            ImageUtil.copyDrawImageToSD(mContext, bitmap, "share_icon");  //
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, ImageUtil.getImagePathFromFileName(mContext, "share_icon"));
        }

        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mContext.getResources().getString(R.string.app_name));
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

        if (!isShareToQZone) {
            doShareToQQ(mContext, params, iShareListener);
        } else {  // 分享到qq空间
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
            doShareToQzone(mContext, params, iShareListener);
        }
    }

    private void doShareToQQ(final Context context, final Bundle params, final IShareListener iShareListener) {
        // QQ分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ((Activity) context, params, new ShareIUiListener(iShareListener));
                }
            }
        });
    }

    private void doShareToQzone(final Context context, final Bundle params, final IShareListener iShareListener) {
        // QQ分享要在主线程做
        ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ((Activity) context, params, new ShareIUiListener(iShareListener));
                }
            }
        });
    }
}
