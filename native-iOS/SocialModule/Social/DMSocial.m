//
//  DMSocial.m
//  DMSys
//
//  Created by zhusheng on 8/18/15.
//  Copyright (c) 2015 zhusheng. All rights reserved.
//

#import "DMSocial.h"
#import <objc/runtime.h>
#import "QQSDK/TencentOpenAPI.framework/Headers/QQApiInterface.h"
#import <AlipaySDK/AlipaySDK.h>

#define ALIPAY_SCHEME @"alipayHengfu" //([NSString stringWithFormat:@"alipayDamaiApp%@", [DMGlobalConfigDictionary stringForKey:@"AppID"]])

NSString * const DMSocialWechatAppIdConfigurationKey = @"WechatAppID";
NSString * const DMSocialWeiboAppKeyConfigurationKey = @"WeiboAppKey";
NSString * const DMSocialQQAppIdConfigurationKey = @"QQAppID";
NSString * const DMSocialAlipaySchemeConfigurationKey = @"AlipayScheme";

NSString * const DMSocialImageShareMessageKey = @"image";
NSString * const DMSocialTitleShareMessageKey = @"title";
NSString * const DMSocialDescriptionShareMessageKey = @"desc";
NSString * const DMSocialUrlShareMessageKey = @"url";

@interface DMSocial () <WXApiDelegate, WeiboSDKDelegate, TencentSessionDelegate>

@property (nonatomic, strong) NSString *wechatAppID;
@property (nonatomic, strong) NSString *weiboAppKey;
@property (nonatomic, strong) NSString *qqAppID;
@property (nonatomic, strong) NSString *alipayScheme;
@property (nonatomic, strong) TencentOAuth *tencentOAuth;

@property (nonatomic, strong) NSMutableDictionary *dictionaryOfCallback;
@property (nonatomic, strong) void (^qqCallback)(NSDictionary *info);

//微信支付回调
@property (nonatomic, strong) void (^wechatPayCallback)(NSString *returnKey, NSString *errorMsg);

@end

@implementation DMSocial

+ (instancetype)sharedInstance
{
    static DMSocial *staticSocialObject = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        staticSocialObject = [[DMSocial alloc] init];
    });
    return staticSocialObject;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _dictionaryOfCallback = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)setConfigInfo:(NSDictionary *)configInfo {
    _configInfo = configInfo;
    //configure wechat
    NSString *wechatAppID = [_configInfo objectForKey:DMSocialWechatAppIdConfigurationKey];
    if (wechatAppID != nil) {
        [WXApi registerApp:wechatAppID withDescription:@""];
        self.wechatAppID = wechatAppID;
    }
    
    //configure weibo
    NSString *weiboAppKey = [_configInfo objectForKey:DMSocialWeiboAppKeyConfigurationKey];
    if (weiboAppKey != nil) {
        [WeiboSDK enableDebugMode:YES];
        if(![WeiboSDK registerApp:weiboAppKey])
            NSLog(@"WeiboSDK Register App failed");
        self.weiboAppKey = weiboAppKey;
    }
    
    //configure qq
    NSString *qqAppID = [_configInfo objectForKey:DMSocialQQAppIdConfigurationKey];
    if (qqAppID != nil) {
        _tencentOAuth = [[TencentOAuth alloc] initWithAppId:qqAppID andDelegate:self];
        self.qqAppID = qqAppID;
    }
  
    //configure alipay
    NSString *alipayScheme = [_configInfo objectForKey:DMSocialAlipaySchemeConfigurationKey];
    self.alipayScheme = alipayScheme;
}

- (NSString *)wechatAppId {
    return self.configInfo[DMSocialWechatAppIdConfigurationKey];
}

- (NSString *)weiboAppKey {
    return self.configInfo[DMSocialWeiboAppKeyConfigurationKey];
}

- (NSString *)QQAppId {
    return self.configInfo[DMSocialQQAppIdConfigurationKey];
}

- (void)handleOpenURL:(NSURL *)url
{
    if ([[url scheme] isEqual:_wechatAppID]) {
        [WXApi handleOpenURL:url delegate:self];
    }
    else if ([[url scheme] isEqual:[[NSString alloc] initWithFormat:@"wb%@", _weiboAppKey]])
    {
        [WeiboSDK handleOpenURL:url delegate:self];
    }
    else if ([[url scheme] isEqual:[[NSString alloc] initWithFormat:@"tencent%@", _qqAppID]])
    {
        [TencentOAuth HandleOpenURL:url];
    }
    else if ([[url scheme] isEqual:self.alipayScheme])
    {
        if ([url.host isEqual:@"safepay"]) {
            [[AlipaySDK defaultService] processOrderWithPaymentResult:url standbyCallback:nil];
        }
    }
}


#pragma mark - WXApiDelegate
- (void)onResp:(BaseResp *)resp
{
    if ([resp isKindOfClass:[SendAuthResp class]]) {
        [self handleWxSendAuthResponse:(SendAuthResp *)resp];
    }
    else if ([resp isKindOfClass:[PayResp class]])
    {
        [self handleWxPayResponse:(PayResp *) resp];
    }
}

#pragma marl - check installation
- (BOOL)isWechatInstalled
{
    return [WXApi isWXAppInstalled];
}

- (BOOL)isQQinstalled
{
    return [TencentOAuth iphoneQQInstalled];
}

- (BOOL)isWeiboInstalled
{
    return [WeiboSDK isWeiboAppInstalled];
}

#pragma mark - 微信支付
- (void)sendWechatPayRequestWithMessage:(NSDictionary *)message completion:(void (^)(NSString *, NSString *))completion
{
    PayReq* req             = [[PayReq alloc] init];
    req.openID              = [message objectForKey:@"appid"];
    req.partnerId           = [message objectForKey:@"partnerid"];
    req.prepayId            = [message objectForKey:@"prepayid"];
    req.nonceStr            = [message objectForKey:@"noncestr"];
    req.timeStamp           = (UInt32)[[message objectForKey:@"timestamp"] integerValue];
    req.package             = [message objectForKey:@"package"];
    req.sign                = [message objectForKey:@"sign"];
    
    _wechatPayCallback = completion;
    [WXApi sendReq:req];
}

- (void) handleWxPayResponse:(PayResp *) resp
{
    if (resp.errCode != WXSuccess) {
        NSString *msg = resp.errCode == WXErrCodeUserCancel ? @"支付失败, 用户取消" : @"支付失败";
        _wechatPayCallback(resp.returnKey, msg);
        return;
    }
    
    _wechatPayCallback(resp.returnKey, nil);
}

#pragma mark - 支付宝支付
- (void)sendAlipayRequestWithOrder:(NSString *)orderStr completion:(void (^)(NSDictionary *, NSString *))completion
{
    [[AlipaySDK defaultService] payOrder:orderStr fromScheme:self.alipayScheme callback:^(NSDictionary *resultDic) {
        NSString *memo = [resultDic objectForKey:@"memo"];
        long resultStatus = [[resultDic objectForKey:@"resultStatus"] integerValue];
        
        NSString *errorMsg = resultStatus == 9000 ? nil : memo;
        completion(resultDic, errorMsg);
    }];
}

#pragma mark - wechat

- (void)sendWechatAuthorizeRequestWithViewController:(UIViewController *)viewController completion:(void (^)(NSString *))completion
{
    SendAuthReq *req = [[SendAuthReq alloc] init];
    req.scope = @"snsapi_message,snsapi_userinfo,snsapi_friend,snsapi_contact"; // @"post_timeline,sns"
    req.state = [[NSUUID UUID] UUIDString];
    if (completion != nil) {
        [_dictionaryOfCallback setObject:completion forKey:req.state];
    }
    req.openID = _wechatAppID;
    [WXApi sendAuthReq:req viewController:viewController delegate:self];
}

- (void) handleWxSendAuthResponse:(SendAuthResp *) resp
{
    void (^completion)(NSString *) = [_dictionaryOfCallback objectForKey:resp.state];
    //remove callback
    if (completion) {
        [_dictionaryOfCallback removeObjectForKey:resp.state];
    }
    
    //handle error
    if (resp.errCode != WXSuccess) {
        NSLog(@"WX ERROR CODE: %d", resp.errCode);
        return;
    }
    
    //call callback
    if (completion) {
        completion(resp.code);
    }
}

#pragma mark - weibo

- (void)sendWeiboAuthorizeRequestCompletion:(void (^)(NSDictionary *))completion
{
    NSString *requestID = [[NSUUID UUID] UUIDString];
    WBAuthorizeRequest *request = [WBAuthorizeRequest request];
    request.redirectURI = @"https://api.weibo.com/oauth2/default.html";
    request.scope = @"all";
    request.userInfo = @{@"state": requestID};
    
    [_dictionaryOfCallback setObject:completion forKey:requestID];
    [WeiboSDK sendRequest:request];
}

- (void)didReceiveWeiboResponse:(WBBaseResponse *)response
{
    NSString *requestID = [response.requestUserInfo objectForKey:@"state"];
    void (^completion)(NSDictionary *) = [_dictionaryOfCallback objectForKey:requestID];
    if (completion) {
        [_dictionaryOfCallback removeObjectForKey:requestID];
    }
    if (response.statusCode != WeiboSDKResponseStatusCodeSuccess) {
        NSLog(@"WEIBO ERROR CODE: %ld", (long)response.statusCode);
        return;
    }
    
    if (completion) {
        completion(response.userInfo);
    }
    
}

#pragma mark - QQ
- (void)sendQQAuthorizeRequestCompletion:(void (^)(NSDictionary *))completion
{
    _qqCallback = completion;
    [_tencentOAuth authorize:@[@"get_user_info", @"get_simple_userinfo"] inSafari:NO];
}

- (void)tencentDidLogin
{
    if (!_qqCallback) {
        return;
    }
    
    _qqCallback(@{@"accessToken":_tencentOAuth.accessToken, @"openID": _tencentOAuth.openId, @"expirationDate": _tencentOAuth.expirationDate});
    _qqCallback = nil;
}

#pragma mark - share Wechat
- (void)sendWechatRequestMessage:(NSDictionary *) message scene:(int) scene
{
    NSData *imageData = nil;
    id imgObj = [message objectForKey:@"image"];
    if ([imgObj isKindOfClass:[UIImage class]]) {
        imageData = UIImagePNGRepresentation(imgObj);
    }
    NSString *title = [message objectForKey:@"title"];
    NSString *desc = [message objectForKey:@"desc"];
    NSString *urlStr = [message objectForKey:@"url"];
    
    WXMediaMessage *wxMessage = [WXMediaMessage message];
    wxMessage.title = title;
    wxMessage.description = desc;
    wxMessage.thumbData = imageData;
    WXWebpageObject *webpageObj = [WXWebpageObject object];
    webpageObj.webpageUrl = urlStr;
    wxMessage.mediaObject = webpageObj;
    
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.message = wxMessage;
    req.bText = NO;
    req.scene = scene;
    [WXApi sendReq:req];
}

- (void)sendWechatSessionRequestWithMessage:(NSDictionary *)message
{
    [self sendWechatRequestMessage:message scene:WXSceneSession];
}

- (void)sendWechatTimelineRequestWithMessage:(NSDictionary *)message
{
    [self sendWechatRequestMessage:message scene:WXSceneTimeline];
}

- (void)sendWechatFavoriteRequestWithMessage:(NSDictionary *)message
{
    [self sendWechatRequestMessage:message scene:WXSceneFavorite];
}

- (void)sendWechatSessionRequestWithImage:(UIImage *)image
{
    if (!image) {
        NSLog(@"分享图片UIImage为nil");
        return;
    }
    
    NSData *imageData = UIImageJPEGRepresentation(image, 0.05f);
    
    WXMediaMessage *wxMessage = [WXMediaMessage message];
    wxMessage.thumbData = UIImagePNGRepresentation(image);
    
    WXImageObject *imageObj = [WXImageObject object];
    imageObj.imageData = imageData;
    wxMessage.mediaObject = imageObj;
    
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.message = wxMessage;
    req.bText = NO;
    req.scene = WXSceneSession;
    [WXApi sendReq:req];
}

#pragma mark - share Weibo
- (void) sendWeiboRequestWithMessage:(NSDictionary *)message
{
    NSData *imageData = nil;
    id imgObj = [message objectForKey:@"image"];
    if ([imgObj isKindOfClass:[UIImage class]]) {
        imageData = UIImagePNGRepresentation(imgObj);
    }
    
    NSString *title = [message objectForKey:@"title"];
    NSString *desc = [message objectForKey:@"desc"];
    NSString *urlStr = [message objectForKey:@"url"];
    
    WBImageObject *wbImageObj = [WBImageObject object];
    wbImageObj.imageData = imageData;
    
    WBMessageObject *msgObj = [WBMessageObject message];
    if (imageData) {
        msgObj.text = [NSString stringWithFormat:@"%@ \n%@ \n%@", title, desc ? desc : @"", urlStr];
        msgObj.imageObject = wbImageObj;
    }
    else {
        WBWebpageObject *webObj = [WBWebpageObject object];
        webObj.objectID = [[NSUUID UUID] UUIDString];
        webObj.webpageUrl = urlStr;
        webObj.title = title;
        webObj.description = desc;
        webObj.thumbnailData = imageData;
        msgObj.text = title;
        msgObj.mediaObject = webObj;
    }
    WBSendMessageToWeiboRequest *req = [WBSendMessageToWeiboRequest requestWithMessage:msgObj];
    [WeiboSDK sendRequest:req];
}

#pragma mark - share QQ
- (void)sendQQShareRequestWithMessage:(NSDictionary *)message
{
    NSData *imageData = nil;
    id imgObj = [message objectForKey:@"image"];
    if ([imgObj isKindOfClass:[UIImage class]]) {
        imageData = UIImagePNGRepresentation(imgObj);
    }
    NSString *title = [message objectForKey:@"title"];
    NSString *desc = [message objectForKey:@"desc"];
    NSString *urlStr = [message objectForKey:@"url"];
    
    QQApiNewsObject *newsObj = [[QQApiNewsObject alloc] initWithURL:[[NSURL alloc] initWithString:urlStr]
                                                              title:title description:desc
                                                    previewImageURL:nil
                                                  targetContentType:QQApiURLTargetTypeNews];
    newsObj.previewImageData = imageData;
    SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:newsObj];
    [QQApiInterface sendReq:req];
}

- (void)sendQZoneShareRequestWithMessage:(NSDictionary *)message
{
    NSData *imageData = nil;
    id imgObj = [message objectForKey:@"image"];
    if ([imgObj isKindOfClass:[UIImage class]]) {
        imageData = UIImagePNGRepresentation(imgObj);
    }
    NSString *title = [message objectForKey:@"title"];
    NSString *desc = [message objectForKey:@"desc"];
    NSString *urlStr = [message objectForKey:@"url"];
    
    QQApiNewsObject *newsObj = [[QQApiNewsObject alloc] initWithURL:[[NSURL alloc] initWithString:urlStr]
                                                              title:title description:desc
                                                    previewImageURL:nil
                                                  targetContentType:QQApiURLTargetTypeNews];
    newsObj.previewImageData = imageData;
    SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:newsObj];
    [QQApiInterface SendReqToQZone:req];
}

@end
