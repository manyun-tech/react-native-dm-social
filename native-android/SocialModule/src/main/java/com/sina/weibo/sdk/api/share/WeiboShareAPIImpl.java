package com.sina.weibo.sdk.api.share;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.sina.weibo.sdk.ApiUtils;
import com.sina.weibo.sdk.WeiboAppManager;
import com.sina.weibo.sdk.WeiboAppManager.WeiboInfo;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.cmd.WbAppActivator;
import com.sina.weibo.sdk.component.ShareRequestParam;
import com.sina.weibo.sdk.component.WeiboSdkBrowser;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.sdk.utils.AidTask;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.MD5;
import com.sina.weibo.sdk.utils.Utility;

class WeiboShareAPIImpl
  implements IWeiboShareAPI
{
  private static final String TAG = WeiboShareAPIImpl.class.getName();
  private Context mContext;
  private String mAppKey;
  private WeiboInfo mWeiboInfo = null;

  private boolean mNeedDownloadWeibo = true;
  private IWeiboDownloadListener mDownloadListener;
  private Dialog mDownloadConfirmDialog = null;

  public WeiboShareAPIImpl(Context context, String appKey, boolean needDownloadWeibo)
  {
    this.mContext = context;
    this.mAppKey = appKey;
    this.mNeedDownloadWeibo = needDownloadWeibo;

    this.mWeiboInfo = WeiboAppManager.getInstance(context).getWeiboInfo();
    if (this.mWeiboInfo != null)
      LogUtil.d(TAG, this.mWeiboInfo.toString());
    else {
      LogUtil.d(TAG, "WeiboInfo is null");
    }
    AidTask.getInstance(context).aidTaskInit(appKey);
  }

  public int getWeiboAppSupportAPI()
  {
    return (this.mWeiboInfo == null) || (!this.mWeiboInfo.isLegal()) ? -1 : this.mWeiboInfo.getSupportApi();
  }

  public boolean isWeiboAppInstalled()
  {
    return (this.mWeiboInfo != null) && (this.mWeiboInfo.isLegal());
  }

  public boolean isWeiboAppSupportAPI()
  {
    return getWeiboAppSupportAPI() >= 10350;
  }

  public boolean isSupportWeiboPay()
  {
    return getWeiboAppSupportAPI() >= 10353;
  }

  public boolean registerApp()
  {
    sendBroadcast(this.mContext, "com.sina.weibo.sdk.Intent.ACTION_WEIBO_REGISTER", this.mAppKey, null, null);
    return true;
  }

  public boolean handleWeiboResponse(Intent intent, IWeiboHandler.Response handler)
  {
    String appPackage = intent.getStringExtra("_weibo_appPackage");
    String transaction = intent.getStringExtra("_weibo_transaction");

    if (TextUtils.isEmpty(appPackage)) {
      LogUtil.e(TAG, "handleWeiboResponse faild appPackage is null");
      return false;
    }

    if (TextUtils.isEmpty(transaction)) {
      LogUtil.e(TAG, "handleWeiboResponse faild intent _weibo_transaction is null");
      return false;
    }

    if ((!ApiUtils.validateWeiboSign(this.mContext, appPackage))) {
      LogUtil.e(TAG, "handleWeiboResponse faild appPackage validateSign faild");
      return false;
    }

    SendMessageToWeiboResponse data = new SendMessageToWeiboResponse(intent.getExtras());
    handler.onResponse(data);
    return true;
  }

  public boolean handleWeiboRequest(Intent intent, IWeiboHandler.Request handler)
  {
    if ((intent == null) || (handler == null)) {
      return false;
    }

    String appPackage = intent.getStringExtra("_weibo_appPackage");
    String transaction = intent.getStringExtra("_weibo_transaction");

    if (TextUtils.isEmpty(appPackage)) {
      LogUtil.e(TAG, "handleWeiboRequest faild appPackage validateSign faild");
      handler.onRequest(null);
      return false;
    }
    if (TextUtils.isEmpty(transaction)) {
      LogUtil.e(TAG, "handleWeiboRequest faild intent _weibo_transaction is null");
      handler.onRequest(null);
      return false;
    }
    if (!ApiUtils.validateWeiboSign(this.mContext, appPackage)) {
      LogUtil.e(TAG, "handleWeiboRequest faild appPackage validateSign faild");
      handler.onRequest(null);
      return false;
    }

    ProvideMessageForWeiboRequest data = new ProvideMessageForWeiboRequest(intent.getExtras());
    handler.onRequest(data);
    return true;
  }

  public boolean launchWeibo(Activity act)
  {
    if (!isWeiboAppInstalled()) {
      LogUtil.e(TAG, "launchWeibo faild WeiboInfo is null");
      return false;
    }
    try
    {
      act.startActivity(
        act.getPackageManager().getLaunchIntentForPackage(this.mWeiboInfo.getPackageName()));
    } catch (Exception e) {
      LogUtil.e(TAG, e.getMessage());
      return false;
    }

    return true;
  }

  public boolean sendRequest(Activity act, BaseRequest request)
  {
    if (request == null) {
      LogUtil.e(TAG, "sendRequest faild request is null");
      return false;
    }
    try
    {
      if (!checkEnvironment(this.mNeedDownloadWeibo))
        return false;
    }
    catch (Exception e) {
      LogUtil.e(TAG, e.getMessage());
      return false;
    }

    if (!request.check(this.mContext, this.mWeiboInfo, new VersionCheckHandler())) {
      LogUtil.e(TAG, "sendRequest faild request check faild");
      return false;
    }
    WbAppActivator.getInstance(this.mContext, this.mAppKey).activateApp();

    Bundle data = new Bundle();
    request.toBundle(data);
    return launchWeiboActivity(act, "com.sina.weibo.sdk.action.ACTION_WEIBO_ACTIVITY", this.mWeiboInfo.getPackageName(), this.mAppKey, data);
  }

  public boolean sendRequest(Activity act, BaseRequest request, AuthInfo authInfo, String token, WeiboAuthListener authListener)
  {
    if (request == null) {
      LogUtil.e(TAG, "sendRequest faild request is null !");
      return false;
    }

    if ((isWeiboAppInstalled()) && (isWeiboAppSupportAPI()))
    {
      int supportApi = getWeiboAppSupportAPI();
      if (supportApi >= 10351) {
        return sendRequest(act, request);
      }
      if ((request instanceof SendMultiMessageToWeiboRequest))
      {
        SendMultiMessageToWeiboRequest multiMessageReq = (SendMultiMessageToWeiboRequest)request;
        SendMessageToWeiboRequest singleMessageReq = new SendMessageToWeiboRequest();
        singleMessageReq.packageName = multiMessageReq.packageName;
        singleMessageReq.transaction = multiMessageReq.transaction;
        singleMessageReq.message = adapterMultiMessage2SingleMessage(multiMessageReq.multiMessage);
        return sendRequest(act, singleMessageReq);
      }

      return sendRequest(act, request);
    }

    return startShareWeiboActivity(act, token, request, authListener);
  }

  private WeiboMessage adapterMultiMessage2SingleMessage(WeiboMultiMessage multiMessage)
  {
    if (multiMessage == null) {
      return new WeiboMessage();
    }
    Bundle data = new Bundle();
    multiMessage.toBundle(data);
    WeiboMessage message = new WeiboMessage(data);
    return message;
  }

  private boolean startShareWeiboActivity(Activity act, String token, BaseRequest request, WeiboAuthListener authListener)
  {
    try {
      WbAppActivator.getInstance(this.mContext, this.mAppKey).activateApp();

      Bundle data = new Bundle();
      String appPackage = act.getPackageName();

      ShareRequestParam param = new ShareRequestParam(act);
      param.setToken(token);
      param.setAppKey(this.mAppKey);
      param.setAppPackage(appPackage);
      param.setBaseRequest(request);
      param.setSpecifyTitle("微博分享");
      param.setAuthListener(authListener);

      Intent intent = new Intent(act, WeiboSdkBrowser.class);
      intent.putExtras(param.createRequestParamBundle());
      act.startActivity(intent);
      return true;
    } catch (ActivityNotFoundException localActivityNotFoundException) {
    }
    return false;
  }

  public boolean sendResponse(BaseResponse response)
  {
    if (response == null) {
      LogUtil.e(TAG, "sendResponse failed response null");
      return false;
    }
    if (!response.check(this.mContext, new VersionCheckHandler())) {
      LogUtil.e(TAG, "sendResponse check fail");
      return false;
    }

    Bundle data = new Bundle();
    response.toBundle(data);
    sendBroadcast(this.mContext, "com.sina.weibo.sdk.Intent.ACTION_WEIBO_RESPONSE", this.mAppKey, response.reqPackageName, data);
    return true;
  }

  private void registerWeiboDownloadListener(IWeiboDownloadListener listener)
  {
    this.mDownloadListener = listener;
  }

  private boolean checkEnvironment(boolean bShowDownloadDialog)
    throws WeiboShareException
  {
    if (!isWeiboAppInstalled()) {
      if (bShowDownloadDialog) {
        if (this.mDownloadConfirmDialog == null) {
          this.mDownloadConfirmDialog = WeiboDownloader.createDownloadConfirmDialog(this.mContext, this.mDownloadListener);
          this.mDownloadConfirmDialog.show();
        }
        else if (!this.mDownloadConfirmDialog.isShowing()) {
          this.mDownloadConfirmDialog.show();
        }

        return false;
      }
      throw new WeiboShareException("Weibo is not installed!");
    }

    if (!isWeiboAppSupportAPI()) {
      throw new WeiboShareException("Weibo do not support share api!");
    }

    if (!ApiUtils.validateWeiboSign(this.mContext, this.mWeiboInfo.getPackageName())) {
      throw new WeiboShareException("Weibo signature is incorrect!");
    }

    return true;
  }

  public boolean launchWeiboPay(Activity act, String payArgs)
  {
    try
    {
      boolean needDownloadWeibo = true;
      if (!checkEnvironment(needDownloadWeibo))
        return false;
    }
    catch (Exception e) {
      LogUtil.e(TAG, e.getMessage());
      return false;
    }

    Bundle bundle = new Bundle();
    bundle.putString("rawdata", payArgs);
    bundle.putInt("_weibo_command_type", 4);
    bundle.putString("_weibo_transaction", String.valueOf(System.currentTimeMillis()));
    return launchWeiboActivity(act, "com.sina.weibo.sdk.action.ACTION_WEIBO_PAY_ACTIVITY", this.mWeiboInfo.getPackageName(), this.mAppKey, bundle);
  }

  private boolean launchWeiboActivity(Activity activity, String action, String pkgName, String appkey, Bundle data)
  {
    if ((activity == null) || 
      (TextUtils.isEmpty(action)) || 
      (TextUtils.isEmpty(pkgName)) || 
      (TextUtils.isEmpty(appkey))) {
      LogUtil.e(TAG, "launchWeiboActivity fail, invalid arguments");
      return false;
    }

    Intent intent = new Intent();
    intent.setPackage(pkgName);
    intent.setAction(action);
    String appPackage = activity.getPackageName();

    intent.putExtra("_weibo_sdkVersion", "0031205000");
    intent.putExtra("_weibo_appPackage", appPackage);
    intent.putExtra("_weibo_appKey", appkey);
    intent.putExtra("_weibo_flag", 538116905);
    intent.putExtra("_weibo_sign", MD5.hexdigest(Utility.getSign(activity, appPackage)));

    if (data != null) {
      intent.putExtras(data);
    }
    try
    {
      LogUtil.d(TAG, "launchWeiboActivity intent=" + intent + ", extra=" + intent.getExtras());
      activity.startActivityForResult(intent, 765);
    } catch (ActivityNotFoundException e) {
      LogUtil.e(TAG, e.getMessage());
      return false;
    }

    return true;
  }

  private void sendBroadcast(Context context, String action, String key, String packageName, Bundle data) {
    Intent intent = new Intent(action);
    String appPackage = context.getPackageName();
    intent.putExtra("_weibo_sdkVersion", "0031205000");
    intent.putExtra("_weibo_appPackage", appPackage);
    intent.putExtra("_weibo_appKey", key);
    intent.putExtra("_weibo_flag", 538116905);
    intent.putExtra("_weibo_sign", MD5.hexdigest(Utility.getSign(context, appPackage)));

    if (!TextUtils.isEmpty(packageName)) {
      intent.setPackage(packageName);
    }

    if (data != null) {
      intent.putExtras(data);
    }

    LogUtil.d(TAG, "intent=" + intent + ", extra=" + intent.getExtras());
    context.sendBroadcast(intent, "com.sina.weibo.permission.WEIBO_SDK_PERMISSION");
  }
}