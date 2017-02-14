package com.damai.social.react;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.damai.social.pay.PayManager;
import com.damai.social.pay.alipay.AlipayResultListener;
import com.damai.social.pay.wx.WXRequestData;
import com.damai.social.share.DMSocialShare;
import com.damai.social.share.DMSocialShareConfig;
import com.damai.social.share.bean.LoginPlatform;
import com.damai.social.share.bean.ShareContent;
import com.damai.social.share.bean.SocialConstants;
import com.damai.social.share.interf.IShareListener;
import com.damai.social.share.interf.PlatformActionListener;
import com.damai.social.share.qq.QQLoginManager;
import com.damai.social.share.util.ToastUtil;
import com.damai.social.share.wechat.DMSocialShareWeChatUtils;
import com.damai.social.share.wechat.WechatLoginManager;
import com.damai.social.share.weibo.WeiboLoginManager;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;

/**
 * Created by weijiang on 16/11/16.
 */

public class SocialManagerModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    private Callback mCallback;
    private WxPayCallbackBroadCast mBroadCast;
    private Callback mLoginCallback;
    private Callback mShareCallback;

    public SocialManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
        reactContext.addActivityEventListener(this);
        //Log.e("-------", "SocialManagerModule");
    }

    @Override
    public String getName() {
        return "SocialManager";
    }

    @Override
    public void initialize() {
        super.initialize();
        //Log.e("-------", "initialize");
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        //Log.e("-------", "onCatalystInstanceDestroy");
    }

    @ReactMethod
    public void payByAlipay(ReadableMap signMap, final Callback callback) {
        String sign = null;
        if (signMap.hasKey("order")) {
            sign = signMap.getString("order");
        }

        if (TextUtils.isEmpty(sign))
            callback.invoke("sign is null");

        PayManager.getInstance().aliPay(getCurrentActivity(), sign, new AlipayResultListener() {

            @Override
            public void onSuccess(String orderNo) {
                callback.invoke(null, orderNo);
            }

            @Override
            public void onFailed(String code) {
                WritableMap event = Arguments.createMap();
                event.putString("message", code);
                callback.invoke(event, code);
            }
        });
    }

    @ReactMethod
    public void payByWechat(ReadableMap readableMap, final Callback callback) {
        mCallback = callback;

        if (mBroadCast == null) {
            mBroadCast = new WxPayCallbackBroadCast();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.damaiapp.pay.wxpay");
            Activity activity = getCurrentActivity();

            if (activity != null) {
                activity.registerReceiver(mBroadCast, intentFilter);
            }

        }

        if (!readableMap.hasKey("appid") || !readableMap.hasKey("noncestr") || !readableMap.hasKey("package") || !readableMap.hasKey("partnerid")
                || !readableMap.hasKey("prepayid") || !readableMap.hasKey("timestamp") || !readableMap.hasKey("sign")) {

            callback.invoke(null, "Error ! Miss required parameter");
            return;

        }

        String appid = readableMap.getString("appid");
        String noncestr = readableMap.getString("noncestr");
        String mPackage = readableMap.getString("package");
        String partnerid = readableMap.getString("partnerid");
        String prepayid = readableMap.getString("prepayid");
        int timestamp = readableMap.getInt("timestamp");
        String sign = readableMap.getString("sign");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(noncestr) || TextUtils.isEmpty(mPackage) || TextUtils.isEmpty(partnerid)
                || TextUtils.isEmpty(prepayid) || timestamp == 0 || TextUtils.isEmpty(sign)) {

            callback.invoke(null, "Error ! required parameter format error");
            return;
        }

        WXRequestData data = new WXRequestData();
        data.mAppid = appid;
        data.mNonceStr = noncestr;
        data.mPackage = mPackage;
        data.mPartnerid = partnerid;
        data.mPrepayid = prepayid;
        data.mTimestamp = timestamp + "";
        data.mSign = sign;

        PayManager.getInstance().wxPay(getCurrentActivity(), data);
    }

    @Override
    public void onHostResume() {
        //Log.e("-------", "onHostResume");
    }

    @Override
    public void onHostPause() {
        //Log.e("-------", "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        //Log.e("-------", "onHostDestroy");
    }

    public class WxPayCallbackBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int code = intent.getIntExtra("code", 0);
            String msg = intent.getStringExtra("msg");
            if (code == 0) {  // 0:支付成功，-2：取消支付   支付失败
                mCallback.invoke(null, "支付成功");
            } else if (code == -2) {
                WritableMap event = Arguments.createMap();
                event.putString("message", "用户取消支付");
                mCallback.invoke(event, "用户取消支付");
            } else {
                WritableMap event = Arguments.createMap();
                event.putString("message", "支付失败");
                mCallback.invoke(event, "支付失败");
            }
        }
    }


    //=============== login share =============

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        DMSocialShare.getInstance().onActivityResultData(requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Intent intent) {
        DMSocialShare.getInstance().onShareWeiboResponse(intent);
    }

//    @ReactMethod
//    public void configure(ReadableMap readableMap, final Callback callback) {
//        if (!readableMap.hasKey("qqAppId") || !readableMap.hasKey("wechatAppId") || !readableMap.hasKey("weiboAppKey")) {
//            callback.invoke(null, "Error ! Miss required parameter");
//            return;
//        }
//
//        String qqAppId = readableMap.getString("qqAppId");
//        String wechatAppId = readableMap.getString("wechatAppId");
//        String weiboAppKey = readableMap.getString("weiboAppKey");
//
//        DMSocialShareConfig dmSocialShareConfig = new DMSocialShareConfig.Builder(getCurrentActivity())
//                .configQQ(qqAppId)
//                .configWeChat(wechatAppId)
//                .configSina(weiboAppKey)
//                .build();
//        DMSocialShare.getInstance().init(dmSocialShareConfig);
//    }

    @ReactMethod
    public void configure(ReadableMap readableMap) {
        if (!readableMap.hasKey("qqAppId") || !readableMap.hasKey("wechatAppId") || !readableMap.hasKey("weiboAppKey")) {
            //callback.invoke(null, "Error ! Miss required parameter");
            ToastUtil.toast(getCurrentActivity(),"Error ! Miss required parameter");
            return;
        }

        String qqAppId = readableMap.getString("qqAppId");
        String wechatAppId = readableMap.getString("wechatAppId");
        String weiboAppKey = readableMap.getString("weiboAppKey");

        DMSocialShareConfig dmSocialShareConfig = new DMSocialShareConfig.Builder(getCurrentActivity())
                .configQQ(qqAppId)
                .configWeChat(wechatAppId)
                .configSina(weiboAppKey)
                .build();
        DMSocialShare.getInstance().init(dmSocialShareConfig);
    }

    @ReactMethod
    public void authorizeByWechat(ReadableMap readableMap, final Callback callback) {
        mLoginCallback = callback;
        WechatLoginManager wechatLoginManager = new WechatLoginManager(getCurrentActivity());
        wechatLoginManager.login(platformActionListener);
    }

    @ReactMethod
    public void authorizeByWeibo(ReadableMap readableMap, final Callback callback) {
        mLoginCallback = callback;
        WeiboLoginManager weiboLoginManager = new WeiboLoginManager(getCurrentActivity());
        weiboLoginManager.login(platformActionListener);
    }

    @ReactMethod
    public void authorizeByQQ(ReadableMap readableMap, final Callback callback) {
        mLoginCallback = callback;
        QQLoginManager qqLoginManager = new QQLoginManager(getCurrentActivity());
        qqLoginManager.login(platformActionListener);
    }

    private PlatformActionListener platformActionListener = new PlatformActionListener() {
        @Override
        public void onComplete(LoginPlatform loginPlatform, HashMap<String, Object> info) {
            if (mLoginCallback != null) {
                mCallback.invoke(null, "未做处理");
            }
        }

        @Override
        public void onError(String errMsg) {
            if (mLoginCallback != null) {
                WritableMap event = Arguments.createMap();
                event.putString("message", "用户取消登录");
                mCallback.invoke(event, "用户取消登录");
            }
        }

        @Override
        public void onCancel() {
            if (mLoginCallback != null) {
                WritableMap event = Arguments.createMap();
                event.putString("message", "登录失败");
                mCallback.invoke(event, "支付失败");
            }
        }
    };

    /**
     * 分享到微信聊天
     */
    @ReactMethod
    public void shareToWechatSession(ReadableMap readableMap, final Callback callback) {
        mShareCallback = callback;
        _Share(1, readableMap);
    }

    /**
     * 分享到微信朋友圈
     */
    @ReactMethod
    public void shareToWechatTimeline(ReadableMap readableMap, final Callback callback) {
        mShareCallback = callback;
        _Share(2, readableMap);
    }

    /**
     * 分享到微信收藏
     */
    @ReactMethod
    public void shareToWechatFavorite(ReadableMap readableMap, final Callback callback) {
        mShareCallback = callback;
        _Share(6, readableMap);
    }


    /**
     * 分享到微博
     */
    @ReactMethod
    public void shareToWeibo(ReadableMap readableMap, final Callback callback) {
        mShareCallback = callback;
        _Share(3, readableMap);
    }

    /**
     * 分享到qq
     */
    @ReactMethod
    public void shareToQQ(ReadableMap readableMap, final Callback callback) {
        mShareCallback = callback;
        _Share(4, readableMap);
    }

    /**
     * 分享到qq空间
     */
    @ReactMethod
    public void shareToQZone(ReadableMap readableMap, final Callback callback) {
        mShareCallback = callback;
        _Share(5, readableMap);
    }

    private void _Share(final int shareType, ReadableMap readableMap) {

        final String title = readableMap.getString("title");
        final String desc = readableMap.getString("desc");
        final String url = readableMap.getString("url");
        String thumbnail = readableMap.getString("thumbnail");
        String web_thumbnail = "";
        if (readableMap.hasKey("web_thumbnail")) {
            web_thumbnail = readableMap.getString("web_thumbnail");
        }

        if (!TextUtils.isEmpty(web_thumbnail)) {
            ShareContent shareContent = new ShareContent();
            shareContent.setTitle(title);
            shareContent.setContent(desc);
            shareContent.setTargetUrl(url);
            shareContent.setImageUrl(web_thumbnail);
            shareByType(shareContent, shareType);
        } else {
            Uri uri = null;
            try {
                uri = Uri.parse(thumbnail);
                // Verify scheme is set, so that relative uri (used by static resources) are not handled.
                if (uri.getScheme() == null) {
                    uri = null;
                }
            } catch (Exception e) {
                thumbnail = null;
                onShareError();
                // ignore malformed uri, then attempt to extract resource ID.
            }
            if (uri == null) {
                uri = getResourceDrawableUri(getReactApplicationContext(), thumbnail);
            }

            if (uri != null) {
                this._getImage(uri, new ResizeOptions(100, 100), new ImageCallback() {
                    @Override
                    public void invoke(@Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            ShareContent shareContent = new ShareContent();
                            shareContent.setTitle(title);
                            shareContent.setContent(desc);
                            shareContent.setTargetUrl(url);
                            shareContent.setBitmap(bitmap);
                            shareByType(shareContent, shareType);
                        } else {
                            onShareError();
                        }
                    }
                });
            } else {
                onShareError();
            }
        }
    }

    private void shareByType(ShareContent shareContent, int shareType) {
        switch (shareType) {
            case 1:
                shareContent.setShareWay(SocialConstants.SHARE_TYPE_WAY_WEB);
                DMSocialShare.getInstance().shareToWeChat(getCurrentActivity(), shareContent, DMSocialShareWeChatUtils.WEIXIN_SHARE_TYPE_TALK, iShareListener);
                break;
            case 2:
                shareContent.setShareWay(SocialConstants.SHARE_TYPE_WAY_WEB);
                DMSocialShare.getInstance().shareToWeChat(getCurrentActivity(), shareContent, DMSocialShareWeChatUtils.WEIXIN_SHARE_TYPE_TIMELINE, iShareListener);
                break;
            case 6:
                shareContent.setShareWay(SocialConstants.SHARE_TYPE_WAY_WEB);
                DMSocialShare.getInstance().shareToWeChat(getCurrentActivity(), shareContent, DMSocialShareWeChatUtils.WEIXIN_SHARE_TYPE_FAVORITE, iShareListener);
                break;
            case 3:
                shareContent.setShareWay(SocialConstants.SHARE_TYPE_WAY_WEB);
                DMSocialShare.getInstance().shareToWeibo(getCurrentActivity(), shareContent, iShareListener);
                break;
            case 4:
                DMSocialShare.getInstance().shareToQQ(getCurrentActivity(), shareContent, false, iShareListener);
                break;
            case 5:
                DMSocialShare.getInstance().shareToQQ(getCurrentActivity(), shareContent, true, iShareListener);
                break;
        }
    }

    private void onShareError() {
        if (mShareCallback != null) {
            WritableMap event = Arguments.createMap();
            event.putString("message", "分享失败");
            mShareCallback.invoke(null, "分享失败");
        }
    }

    private IShareListener iShareListener = new IShareListener() {
        @Override
        public void onComplete() {
            if (mShareCallback != null) {
                mShareCallback.invoke(null, "分享成功");
            }
        }

        @Override
        public void onError() {
            if (mShareCallback != null) {
                WritableMap event = Arguments.createMap();
                event.putString("message", "分享失败");
                mShareCallback.invoke(event, "分享失败");
            }
        }

        @Override
        public void onCancel() {
            if (mShareCallback != null) {
                WritableMap event = Arguments.createMap();
                event.putString("message", "用户取消分享");
                mShareCallback.invoke(event, "用户取消分享");
            }
        }
    };


    private void _getImage(Uri uri, ResizeOptions resizeOptions, final ImageCallback imageCallback) {
        BaseBitmapDataSubscriber dataSubscriber = new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                bitmap = bitmap.copy(bitmap.getConfig(), true);
                imageCallback.invoke(bitmap);
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                imageCallback.invoke(null);
                dataSource.close();
            }
        };

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (resizeOptions != null) {
            builder = builder.setResizeOptions(resizeOptions);
        }
        ImageRequest imageRequest = builder.build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    private static Uri getResourceDrawableUri(Context context, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        name = name.toLowerCase().replace("-", "_");
        int resId = context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());

        if (resId == 0) {
            return null;
        } else {
            return new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
        }
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

}
