package com.damai.social.share.qq;

import com.damai.social.share.interf.IShareListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

/**
 * ClassName: LoginIUiListener
 * Description: qq 分享回调监听
 */

public class ShareIUiListener implements IUiListener {

    private IShareListener iShareListener;

    public ShareIUiListener() {
    }

    public ShareIUiListener(IShareListener iShareListener) {
        this.iShareListener = iShareListener;
    }

    @Override
    public void onComplete(Object o) {
        if (iShareListener != null){
            iShareListener.onComplete();
        }
        //EventBus.getDefault().post(new BaseEvent.ShareSuccessEvent());
    }

    @Override
    public void onError(UiError uiError) {
        if (iShareListener != null){
            iShareListener.onError();
        }
        //EventBus.getDefault().post(new BaseEvent.ShareFailedEvent());
    }

    @Override
    public void onCancel() {
        if (iShareListener != null){
            iShareListener.onCancel();
        }
        //EventBus.getDefault().post(new BaseEvent.ShareCancelEvent());
    }
}
