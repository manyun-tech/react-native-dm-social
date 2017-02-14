package com.damai.social.share.qq;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.damai.social.share.DMSocialShare;
import com.damai.social.R;
import com.damai.social.share.bean.SocialConstants;
import com.damai.social.share.interf.ILoginManager;
import com.damai.social.share.interf.PlatformActionListener;
import com.damai.social.share.util.ToastUtil;
import com.tencent.tauth.Tencent;

/**
 * QQ登录管理类
 */

public class QQLoginManager implements ILoginManager {

    private Context mContext;
    private String mAppId;
    private Tencent mTencent;

    public QQLoginManager(Context context) {
        if (isInstallQQ(context)){
            mContext = context;
            mAppId = DMSocialShare.getInstance().getConfig().qqAppId;
            if (!TextUtils.isEmpty(mAppId)) {
                mTencent = Tencent.createInstance(mAppId, context);
            }
        } else {
            ToastUtil.toast(context, R.string.install_qq_tips);
        }
    }

    @Override
    public void login(PlatformActionListener platformActionListener) {
        if (mTencent == null)
            return;

        if (!mTencent.isSessionValid()) {
            mTencent.login((Activity) mContext, SocialConstants.QQ_SCOPE, new LoginIUiListener(platformActionListener));
        } else {
            mTencent.logout(mContext);
        }
    }

    public void qqlogout() {
        mTencent.logout(mContext);
    }

  // com.tencent.qq  com.sina.weibo
  public static boolean isInstallQQ(Context context) {
        return context.getPackageManager().getLaunchIntentForPackage("com.tencent.mobileqq") != null;
  }

}
