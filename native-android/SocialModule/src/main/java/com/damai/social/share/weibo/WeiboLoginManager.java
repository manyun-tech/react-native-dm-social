package com.damai.social.share.weibo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.damai.social.share.DMSocialShare;
import com.damai.social.share.bean.LoginPlatform;
import com.damai.social.share.bean.SocialConstants;
import com.damai.social.share.interf.ILoginManager;
import com.damai.social.share.interf.PlatformActionListener;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import java.util.HashMap;

public class WeiboLoginManager implements ILoginManager {
    private static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    // private static final String REDIRECT_URL = "http://";  // 需与后台的授权回调页配置一致
    private static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"+
            "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog";

    private Context mContext;
    private PlatformActionListener mPlatformActionListener;
    /**
     * 注意：SsoHandler 仅当 SDK 支持 SSO 时有效
     */
    private static SsoHandler mSsoHandler;

    public WeiboLoginManager(Context context) {
        mContext = context;
    }

    public static SsoHandler getSsoHandler() {
        return mSsoHandler;
    }

    @Override
    public void login(PlatformActionListener platformActionListener) {
        mPlatformActionListener = platformActionListener;
        AuthInfo mAuthInfo = new AuthInfo(mContext, DMSocialShare.getInstance().getConfig().sinaAppKey, REDIRECT_URL, SCOPE);
        mSsoHandler = new SsoHandler((Activity) mContext, mAuthInfo);
        // ALL IN ONE 登录方式 ，当没安装客户端时，会自动调用网页登录
        mSsoHandler.authorize(new AuthListener());
    }

    /**
     * * 1. SSO 授权时，需要在 onActivityResult 中调用 {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非SSO 授权时，当授权结束后，该回调就会被执行
     */
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle bundle) {
            final Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (accessToken != null && accessToken.isSessionValid()) {
                String uid = accessToken.getUid();
                String token = accessToken.getToken();
                HashMap<String, Object> userInfoHashMap = new HashMap<>();
                userInfoHashMap.put(SocialConstants.PARAMS_UID, uid);
                userInfoHashMap.put(SocialConstants.PARAMS_ACCESS_TOKEN, token);
                if (mPlatformActionListener != null) {
                    mPlatformActionListener.onComplete(LoginPlatform.WEIBO,userInfoHashMap);
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            if (mPlatformActionListener != null) {
                mPlatformActionListener.onError(e.getMessage());
            }
        }

        @Override
        public void onCancel() {
            if (mPlatformActionListener != null) {
                mPlatformActionListener.onCancel();
            }
        }
    }
}
