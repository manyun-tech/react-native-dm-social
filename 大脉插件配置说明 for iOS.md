# dm-social
### 1.执行命令

>npm install git+ssh://git@gitlab.damaiapp.com:h5-mobile/react-native-dm-social.git --save

记得确保有clone这个项目的权限

### 2.打开`./node_modules/react-native-dm-social`,将`SocialModule`文件夹拖入Xcode工程里面

![image](http://note.youdao.com/yws/api/group/26315757/file/129209290?method=getImage&width=1280&height=10000000&version=1&cstk=rJXi1ZBf)

### 3.Xcode打开Build Phases,在`Link Binary With Libraries`中添加以下库  

libc++.tbd  
libz.tbd  
SystemConfiguration.framework  
CoreTelephony.framework  
QuartzCore.framework  
CoreText.framework  
CoreGraphics.framework  
UIKit.framework  
Foundation.framework  
CFNetwork.framework  
CoreMotion.framework  
libsqlite3.0.tbd

### 4.在Xcode中Info的`URL Types`中添加相应的`URL Schemes`

### 5.在Xcode的Build Settings中将`Enable Bitcode` 设置为 `No`

![image](http://note.youdao.com/yws/api/group/26315757/file/129209289?method=getImage&width=1280&height=10000000&version=1&cstk=rJXi1ZBf)

### 6.在程序中初始化

```js
import SocialManager from 'react-native-dm-social';

...

let config = {
	wechatAppId: 'wxffbb158f276a034b',
	weiboAppKey: '3330737535',
	qqAppId: '1105526408',
	alipayScheme: 'alipayXiaobailejia'
}
//注册大脉社交相关的id
SocialManager.configure(config);
```

### 7.调用API

```js
import SocialManager from 'react-native-dm-social';

...

//支付宝
let params = {
    "order": "xxxxx",
}

SocialManager.payByAlipay(params, callback);

//微信
let params = {
    "appid": "wxffbb158f276a034b",
    "noncestr": "aa771dcc824388f2f116315cded8c0a0",
    "package": "Sign=WXPay",
    "partnerid": "1273708801",
    "prepayid": "wx20161102150344b47beb16550381392207",
    "timestamp": 1478070323,
    "sign": "F5AE11FA027F6F336EDC92AE92B90539"
}

SocialManager.payByWechat(params, (err, resp)=> {
    if (err) {
      //toastMsg(err.message);
      return;
    }
    //toastMsg('支付成功');
});

```

### 8.分享

将Info.plist以Source Code的形式打开，将下面一串复制进去
```xml
<key>LSApplicationQueriesSchemes</key>
<array>
	<string>mqqOpensdkSSoLogin</string>
	<string>mqzone</string>
	<string>sinaweibo</string>
	<string>weibosdk2.5</string>
	<string>weibosdk</string>
	<string>alipayauth</string>
	<string>alipay</string>
	<string>safepay</string>
	<string>mqq</string>
	<string>mqqapi</string>
	<string>mqqopensdkapiV3</string>
	<string>mqqopensdkapiV2</string>
	<string>mqqapiwallet</string>
	<string>mqqwpa</string>
	<string>mqqbrowser</string>
	<string>wtloginmqq2</string>
	<string>weixin</string>
	<string>wechat</string>
</array>
```

API调用如下
```js
import SocialManager from 'react-native-dm-social';

...

let message = {
  title:'女装拼团APP',
  desc: '一手货源,天天上新,档口公开,最新同步,买手把关,剔除差版.',
  url: ${YOUR_SHARE_URL},
  thumbnail: resolveAssetSource(require('../../img/icon-coin.png')).uri, //缩略图
  //thumbnail: "http://up.qqjia.com/z/25/tu32695_14.jpg", //缩略图如果是网络图片则直接填图片URL,图片格式为png或者jpg
  // web_thumbnail:"http://up.qqjia.com/z/25/tu32695_14.jpg" //for android
}

isIos ? SocialManager.shareToWechatSession(message) : SocialManager.shareToWechatSessionAndroid(message);

//isIos ? SocialManager.shareToWechatTimeline(message) : SocialManager.shareToWechatTimelineAndroid(message);

//isIos ? SocialManager.shareToQQ(message) : SocialManager.shareToQQAndroid(message);

//isIos ? SocialManager.shareToWeibo(message) : SocialManager.shareToWeiboAndroid(message);
```