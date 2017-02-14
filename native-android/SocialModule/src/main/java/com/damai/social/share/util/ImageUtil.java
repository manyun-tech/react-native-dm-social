package com.damai.social.share.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.damai.social.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ImageUtil {

    public static String imageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/DMSocialShare/Common/";  // 通用图片路径

    public static void copyDrawImageToSD(Context context, int resId, String imageName) {
        Bitmap bitMap = BitmapFactory.decodeResource(context.getResources(), resId);
        copyDrawImageToSD(context,bitMap,imageName);
    }

    public static void copyDrawImageToSD(Context context, Bitmap bitmap, String imageName) {

        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            File savedir = new File(imageDirectory);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }

            File mFile2 = new File(savedir, imageName + ".png");
//            if (mFile2.exists())
//                return;
            try {
                FileOutputStream outStream = new FileOutputStream(mFile2);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.toast(context, R.string.sd_tips);
        }
    }

    /**
     * 返回图片路径
     */
    public static String getImagePathFromFileName(Context context, String imageName) {
        if (TextUtils.isEmpty(imageName))
            throw new NullPointerException("file name is empty");

        File savedir = new File(imageDirectory + File.separator + imageName + ".png");
        return savedir.exists() ? savedir.getAbsolutePath() : "";
    }

    public static void getHtmlByteArray(final String url, final OnloadImageListener onloadImageListener) {
        new Thread(new Runnable() {  // 需在子线程中现在图片
            @Override
            public void run() {
                try {
                    URL htmlUrl = new URL(url);
                    URLConnection connection = htmlUrl.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    int responseCode = httpConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inStream = httpConnection.getInputStream();
                        onloadImageListener.loadSuccess(inStream);
                    } else {
                        onloadImageListener.loadFailed();
                    }
                } catch (IOException e) {
                    onloadImageListener.loadFailed();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static byte[] inputStreamToByte(InputStream is) {
        try {
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                bytestream.write(ch);
            }
            byte imgdata[] = bytestream.toByteArray();
            bytestream.close();
            return imgdata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }


    public static interface OnloadImageListener {
        void loadSuccess(InputStream inStream);

        void loadFailed();
    }
}
