package com.damai.social.share.qq;

import com.damai.social.share.bean.LoginPlatform;
import com.damai.social.share.bean.SocialConstants;
import com.damai.social.share.interf.PlatformActionListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * ClassName: LoginIUiListener
 * Description: qq 登录回调监听
 */
public class LoginIUiListener implements IUiListener {

    private PlatformActionListener mPlatformActionListener;

    public LoginIUiListener(){

    }

    public LoginIUiListener(PlatformActionListener platformActionListener) {
        this.mPlatformActionListener = platformActionListener;
    }

    @Override
    public void onComplete(Object o) {
        JSONObject jsonObject = (JSONObject) o;
        try {
            String openid = jsonObject.getString(SocialConstants.PARAMS_OPENID);
            String accessToken = jsonObject.getString(SocialConstants.PARAMS_ACCESS_TOKEN);
            HashMap<String, Object> userInfoHashMap = new HashMap<>();
            userInfoHashMap.put(SocialConstants.PARAMS_OPENID, openid);
            userInfoHashMap.put(SocialConstants.PARAMS_ACCESS_TOKEN, accessToken);
            if (mPlatformActionListener != null) {
                mPlatformActionListener
                        .onComplete(LoginPlatform.QQ,userInfoHashMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(UiError uiError) {
        if (mPlatformActionListener != null) {
            mPlatformActionListener
                    .onError(uiError.errorMessage);
        }
    }

    @Override
    public void onCancel() {
        if (mPlatformActionListener != null) {
            mPlatformActionListener
                    .onCancel();
        }
    }
}
