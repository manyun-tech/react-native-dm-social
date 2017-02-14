package com.damai.social.share.interf;

import com.damai.social.share.bean.LoginPlatform;

import java.util.HashMap;

public interface PlatformActionListener {

    /**
     * 登录成功
     */
    void onComplete(LoginPlatform loginPlatform, HashMap<String, Object> info);

    /**
     * 登录失败
     * @param errMsg 错误信息
     */
    void onError(String errMsg);

    /**
     * 取消登录
     */
    void onCancel();


}
