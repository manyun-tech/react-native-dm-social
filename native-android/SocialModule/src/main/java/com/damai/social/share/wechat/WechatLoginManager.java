package com.damai.social.share.wechat;

import android.content.Context;
import android.text.TextUtils;

import com.damai.social.share.DMSocialShare;
import com.damai.social.R;
import com.damai.social.share.interf.ILoginManager;
import com.damai.social.share.interf.PlatformActionListener;
import com.damai.social.share.util.ToastUtil;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WechatLoginManager implements ILoginManager {
    private static final String SCOPE = "snsapi_userinfo";
    private static final String STATE = "damaiapp_wechat_login";

    private static IWXAPI mIWXAPI;
    private static PlatformActionListener mPlatformActionListener;

    public WechatLoginManager(Context context) {
        String weChatAppId = DMSocialShare.getInstance().getConfig().wechatAppId;
        if (!TextUtils.isEmpty(weChatAppId)) {
            mIWXAPI = WXAPIFactory.createWXAPI(context, weChatAppId, true);
            if (mIWXAPI.isWXAppInstalled()) {
                mIWXAPI.registerApp(weChatAppId);
            } else {
                ToastUtil.toast(context, R.string.install_wechat_tips);
            }
        }
    }

    public static IWXAPI getIWXAPI() {
        return mIWXAPI;
    }

    public static PlatformActionListener getPlatformActionListener() {
        return mPlatformActionListener;
    }

    @Override
    public void login(PlatformActionListener platformActionListener) {
        if (mIWXAPI != null) {
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = SCOPE;
            req.state = STATE;
            mIWXAPI.sendReq(req);
            mPlatformActionListener = platformActionListener;
        }
    }
}
