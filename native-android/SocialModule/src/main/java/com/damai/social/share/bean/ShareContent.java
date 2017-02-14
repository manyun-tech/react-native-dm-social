package com.damai.social.share.bean;

import android.graphics.Bitmap;

/**
 * 分享内容实体类
 * Created by weijiang on 16/11/22.
 */

public class ShareContent {

    private String title;   // 标题
    private String content;  // 内容
    private String targetUrl;  // 分享链接
    private String imageUrl;  // 分享的图片url
    private int imageResource;  // 图片资源id
    private Bitmap bitmap;  // 图片bitmap
    private String shareWay;  // 分享方式

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getShareWay() {
        return shareWay;
    }

    public void setShareWay(String shareWay) {
        this.shareWay = shareWay;
    }
}
