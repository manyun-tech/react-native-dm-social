package com.damai.social.share.wechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.damai.social.share.DMSocialShare;
import com.damai.social.share.bean.LoginPlatform;
import com.damai.social.share.bean.SocialConstants;
import com.damai.social.share.interf.IShareListener;
import com.damai.social.share.interf.PlatformActionListener;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.HashMap;

public class WechatHandlerActivity extends Activity implements IWXAPIEventHandler {
    /**
     * BaseResp的getType函数获得的返回值，1:第三方授权， 2:分享
     */
    private static final int TYPE_LOGIN = 1;
    private static final int TYPE_SHARE= 2;

    private IWXAPI mIWXAPI;
    private PlatformActionListener mPlatformActionListener;
    private IShareListener iShareListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIWXAPI = WXAPIFactory.createWXAPI(this, DMSocialShare.getInstance().getConfig().wechatAppId, true);
        mIWXAPI.handleIntent(getIntent(), this);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mIWXAPI != null) {
            mIWXAPI.handleIntent(getIntent(), this);
        }
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == TYPE_LOGIN) {
            mPlatformActionListener = WechatLoginManager
                    .getPlatformActionListener();
        } else if (baseResp.getType() == TYPE_SHARE){
            iShareListener = DMSocialShareWeChatUtils.getIShareListener();
        }

        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (baseResp.getType() == TYPE_LOGIN) {
                    final String code = ((SendAuth.Resp) baseResp).code;
                    HashMap<String, Object> info = new HashMap<>();
                    info.put(SocialConstants.PARAMS_CODE, code);
                    if (mPlatformActionListener != null) {
                        mPlatformActionListener.onComplete(LoginPlatform.WECHAT,info);
                    }
                } else if (baseResp.getType() == TYPE_SHARE) {
                    if (iShareListener != null) {
                        iShareListener.onComplete();
                    }
                    //EventBus.getDefault().post(new BaseEvent.ShareSuccessEvent());
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if (baseResp.getType() == TYPE_LOGIN) {
                    if (mPlatformActionListener != null) {
                        mPlatformActionListener
                                .onCancel();
                    }
                } else if (baseResp.getType() == TYPE_SHARE) {
                    if (iShareListener != null) {
                        iShareListener.onCancel();
                    }
                    //EventBus.getDefault().post(new BaseEvent.ShareCancelEvent());
                }
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                if (baseResp.getType() == TYPE_LOGIN) {
                    if (mPlatformActionListener != null) {
                        mPlatformActionListener
                                .onError("登录失败，重新登录");
                    }
                } else if (baseResp.getType() == TYPE_SHARE) {
                    if (iShareListener != null) {
                        iShareListener.onError();
                    }
                    //EventBus.getDefault().post(new BaseEvent.ShareFailedEvent());
                }
                break;
        }
        finish();
    }
}
