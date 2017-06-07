
import {
  NativeModules
} from 'react-native';

let SocialManager = NativeModules.SocialManager;

export default {
  /**
  params: {
    wechatAppId: 'wx5a9031a93a465882',
    weiboAppKey: '2697181701',
    qqAppId: '',
    alipayScheme: 'alipayYml'
  }
  */
  configure: (params) => {
    SocialManager.configure(params);
  },

  /**
  params: { order: 'xxxxxxxxxxxxx' }
  callback: (err, response) => {},  err message:  err.message
  */
  payByAlipay: (params, callback) => {
    SocialManager.payByAlipay(params, callback);
  },

  /**
  params: {
      "appid": "wxffbb158f276a034b",
      "noncestr": "aa771dcc824388f2f116315cded8c0a0",
      "package": "Sign=WXPay",
      "partnerid": "1273708801",
      "prepayid": "wx20161102150344b47beb16550381392207",
      "timestamp": 1478070223,
      "sign": "F5AE11FA027F6F336EDC92AE92B90539"
    }
  callback: (err, returnKey) => {},  err message: err.message
  */
  payByWechat: (params, callback) => {
    SocialManager.payByWechat(params, callback);
  },

  /**
  callback: (err, code) => {}
  */
  authorizeByWechat: (callback) => {
    SocialManger.authorizeByWechat(callback);
  },

  /**
  callback: (err, info) => {}
  */
  authorizeByWeibo: (callback) => {
    SocialManager.authorizeByWeibo(callback);
  },

  /**
  callback: (err, info) => {}
  */
  authorizeByQQ: (callback) => {
    SocialManager.authorizeByQQ(callback);
  },

  /**
  分享到微信聊天
  message: {
      title:'标题',
      'desc': '描述',
      url:'http://www.damaiapp.com',
      thumbnail: resolveAssetSource(require('../../resources/back.png')).uri //缩略图
    }
  */
  shareToWechatSession: (message) => {
    SocialManager.shareToWechatSession(message);
  },

  /**
  分享到微信朋友圈
  */
  shareToWechatTimeline: (message) => {
    SocialManager.shareToWechatTimeline(message);
  },

  /**
  分享到微信收藏
  */
  shareToWechatFavorite: (message) => {
    SocialManager.shareToWechatFavorite(message);
  },

  /**
  分享到微博
  */
  shareToWeibo: (message) => {
    SocialManager.shareToWeibo(message);
  },

  /**
  分享到QQ
  */
  shareToQQ: (message) => {
    SocialManager.shareToQQ(message);
  },

  /**
  分享到QQ空间
  */
  shareToQZone: (message) => {
    SocialManager.shareToQZone(message);
  },

    /**
     * 检查微信安装
     */
    checkWechatInstallation:(callback)=>{
      SocialManager.checkWechatInstallation(callback);
    },

        /**
     * 检查QQ安装
     */
    checkQQInstallation:(callback)=>{
      SocialManager.checkQQInstallation(callback);
    },


  // =============android 分享 ===========
  shareToQQAndroid: (params, callback) => {
        SocialManager.shareToQQ(params, callback);
  },

  shareToWechatSessionAndroid: (params, callback) => {
        SocialManager.shareToWechatSession(params, callback);
  },

  shareToWechatTimelineAndroid: (params, callback) => {
      SocialManager.shareToWechatTimeline(params, callback);
  },

  shareToWechatFavoriteAndroid: (params, callback) => {
      SocialManager.shareToWechatFavorite(params, callback);
  },

  shareToQZoneAndroid: (params, callback) => {
       SocialManager.shareToQZone(params, callback);
  },

  shareToWeiboAndroid: (params, callback) => {
       SocialManager.shareToWeibo(params, callback);
  },

};
