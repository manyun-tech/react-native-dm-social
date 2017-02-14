package com.damai.social.pay;


import android.app.Activity;

import com.damai.social.pay.alipay.AlipayResultListener;
import com.damai.social.pay.alipay.AlipayUtil;
import com.damai.social.pay.wx.WXPayUtil;
import com.damai.social.pay.wx.WXRequestData;


public class PayManager {

    private static PayManager mInstance = new PayManager();

    public static PayManager getInstance() {
        return mInstance;
    }

    //银联
//    public void unionPay(Activity activity, String tradeNo) {
//
//        UnionPayUtil pay = new UnionPayUtil();
//        pay.pay(activity, tradeNo);
//    }

    //支付宝
    public void aliPay(Activity activity, String orderInfo, AlipayResultListener listener) {
        AlipayUtil ali = new AlipayUtil(activity, listener);
        ali.pay(orderInfo);
    }

    //微信
    public void wxPay(Activity activity, WXRequestData data) {
        WXPayUtil wx = new WXPayUtil(activity);
        wx.pay(data);
    }
}
