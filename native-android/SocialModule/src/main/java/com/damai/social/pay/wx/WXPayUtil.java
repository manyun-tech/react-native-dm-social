package com.damai.social.pay.wx;

import android.app.Activity;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayUtil {
	PayReq req;
	final IWXAPI msgApi;

	public WXPayUtil(Activity activity) {
		req = new PayReq();
		msgApi = WXAPIFactory.createWXAPI(activity, null);
		msgApi.registerApp(Constants.APP_ID);
	}

	public void pay(WXRequestData data) {
		req.appId = data.mAppid;
		req.partnerId = data.mPartnerid;
		req.prepayId = data.mPrepayid;
		req.packageValue = data.mPackage;
		req.nonceStr = data.mNonceStr;
		req.timeStamp = data.mTimestamp;
		req.sign = data.mSign;

		sendPayReq();
	}

	private void sendPayReq() {
		msgApi.registerApp(Constants.APP_ID);
		msgApi.sendReq(req);
	}
}

