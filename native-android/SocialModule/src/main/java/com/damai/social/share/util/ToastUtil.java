package com.damai.social.share.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void toast(Context context, String msg) {
        //Toaster.toast(context.getString(R.string.install_wechat_tips));
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toast(Context context, int msgResId) {
        String msg = context.getResources().getString(msgResId);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
