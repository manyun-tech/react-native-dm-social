package com.damai.social.share;

import android.content.Context;
import android.content.Intent;

import com.damai.social.share.bean.ShareContent;
import com.damai.social.share.interf.IShareListener;
import com.damai.social.share.qq.DMSocialShareQQUtils;
import com.damai.social.share.qq.LoginIUiListener;
import com.damai.social.share.qq.ShareIUiListener;
import com.damai.social.share.wechat.DMSocialShareWeChatUtils;
import com.damai.social.share.weibo.DMSocialShareWeiboUtils;
import com.damai.social.share.weibo.WeiboLoginManager;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.Tencent;

/**
 * 分享配置
 * Created by weijiang on 16/11/22.
 */

public class DMSocialShare {

    private volatile static DMSocialShare instance;
    private DMSocialShareConfig mConfig;

    public static DMSocialShare getInstance() {
        if (instance == null) {
            synchronized (DMSocialShare.class) {
                if (instance == null) {
                    instance = new DMSocialShare();
                }
            }
        }
        return instance;
    }

    public void init(DMSocialShareConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("DMSocialShareConfig is not null");
        }
        mConfig = config;
    }

    public DMSocialShareConfig getConfig() {
        return mConfig;
    }

    public void shareToQQ(Context context, ShareContent shareContentBean, boolean isShareToQZone, IShareListener iShareListener) {
        DMSocialShareQQUtils utils = DMSocialShareQQUtils.getInstance();
        boolean isInit = utils.init(context);
        if (isInit)
            utils.shareToQQ(shareContentBean, isShareToQZone, iShareListener);
    }

    public void shareToWeChat(Context context, ShareContent shareContentBean, int shareType, IShareListener iShareListener) {
        DMSocialShareWeChatUtils utils = DMSocialShareWeChatUtils.getInstance();
        boolean isInit = utils.init(context);
        if (isInit)
            utils.shareToWeChat(shareContentBean, shareType, iShareListener);
    }

    public void shareToWeibo(Context context, ShareContent shareContentBean, IShareListener iShareListener) {
        DMSocialShareWeiboUtils utils = DMSocialShareWeiboUtils.getInstance();
        boolean isInit = utils.init(context);
        if (isInit)
            utils.shareToWeibo(context, shareContentBean, iShareListener);
    }

    /**
     * 登录分享回调
     */
    public void onActivityResultData(int requestCode, int resultCode, Intent data) {
        // qq登录
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN ||
                requestCode == com.tencent.connect.common.Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, new LoginIUiListener());
        }

        SsoHandler ssoHandler = WeiboLoginManager.getSsoHandler();
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

        // qq分享
        if (requestCode == Constants.REQUEST_QQ_SHARE ||
                requestCode == Constants.REQUEST_QZONE_SHARE) {
            Tencent.onActivityResultData(requestCode, resultCode, data, new ShareIUiListener());
        }

    }

    /**
     * 微博分享onNewIntent回调
     */
    public void onShareWeiboResponse(Intent intent) {
        DMSocialShareWeiboUtils utils = DMSocialShareWeiboUtils.getInstance();
        utils.response(intent);
    }

}
