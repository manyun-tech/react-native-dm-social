package com.damai.social.pay.alipay;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

public class AlipayUtil {

	// 支付宝公钥
	public static final String RSA_PUBLIC = "";
	private static final int SDK_PAY_FLAG = 1;

	private static final int SDK_CHECK_FLAG = 2;

	private Activity mActivity;
	private AlipayResultListener mListener;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SDK_PAY_FLAG: {
					PayResult payResult = new PayResult((String) msg.obj);

					// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
					String resultInfo = payResult.getResult();

					String resultStatus = payResult.getResultStatus();

					// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
					if (TextUtils.equals(resultStatus, "9000")) {
//					String tradeNo = "";
//
//					try {
//						String tradeNoPat = "out_trade_no=\"";
//						int tradeNoStart = resultInfo.indexOf(tradeNoPat);
//						if(tradeNoStart >= 0) {
//							String ret1 = resultInfo.substring(tradeNoStart + tradeNoPat.length());
//							int end = ret1.indexOf("\"");
//							tradeNo = ret1.substring(0, end);
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//						tradeNo = "";
//					}

						if (mListener != null) {
							mListener.onSuccess("支付成功");
						}
						//Toast.makeText(mActivity, "支付成功", Toast.LENGTH_SHORT).show();
						//Toaster.toast("支付成功");
					} else {
						// 判断resultStatus 为非“9000”则代表可能支付失败
						// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
						if (TextUtils.equals(resultStatus, "8000")) {
							// Toast.makeText(mActivity, "支付结果确认中", Toast.LENGTH_SHORT).show();
							//Toaster.toast("支付结果确认中");
						} else if(TextUtils.equals(resultStatus, "6001")){
							mListener.onFailed("用户取消支付");
						}else{
							// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
							//Toast.makeText(mActivity, "支付失败: "+payResult.getMemo(), Toast.LENGTH_SHORT).show();
							//Toaster.toast("支付失败: "+payResult.getMemo());
							mListener.onFailed("支付失败");
						}
					}
					break;
				}
				case SDK_CHECK_FLAG: {
					Toast.makeText(mActivity, "检查结果为：" + msg.obj,
							Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					break;
			}
		}
	};


	public AlipayUtil(Activity activity, AlipayResultListener listener) {
		mActivity = activity;
		mListener = listener;
	}

	/**
	 * call alipay sdk pay. 调用SDK支付
	 *
	 */
	public void pay(final String payInfo) {
		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(mActivity);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

}
