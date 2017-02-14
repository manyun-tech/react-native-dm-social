//
//  DMSocial.h
//  DMSys
//
//  Created by zhusheng on 8/18/15.
//  Copyright (c) 2015 zhusheng. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "WXApi.h"
#import "WeiboSDK.h"
#import <TencentOpenAPI/TencentOAuth.h>

extern NSString * const DMSocialWechatAppIdConfigurationKey;
extern NSString * const DMSocialWeiboAppKeyConfigurationKey;
extern NSString * const DMSocialQQAppIdConfigurationKey;
extern NSString * const DMSocialAlipaySchemeConfigurationKey;

extern NSString * const DMSocialImageShareMessageKey;
extern NSString * const DMSocialTitleShareMessageKey;
extern NSString * const DMSocialDescriptionShareMessageKey;
extern NSString * const DMSocialUrlShareMessageKey;

@interface DMSocial : NSObject

@property (nonatomic, strong) NSDictionary *configInfo;

+ (instancetype) sharedInstance;

- (NSString *) wechatAppId;
- (NSString *) weiboAppKey;
- (NSString *) QQAppId;

- (BOOL) isWechatInstalled;
- (BOOL) isWeiboInstalled;
- (BOOL) isQQinstalled;

- (void) handleOpenURL:(NSURL *) url;

//authorize
- (void) sendWechatAuthorizeRequestWithViewController:(UIViewController *) viewController completion:(void (^)(NSString *wxCode)) completion;
- (void) sendWeiboAuthorizeRequestCompletion:(void (^)(NSDictionary *info)) completion;
- (void) sendQQAuthorizeRequestCompletion:(void (^)(NSDictionary *info)) completion;

//share
- (void) sendWechatSessionRequestWithMessage:(NSDictionary *) message;
- (void) sendWechatTimelineRequestWithMessage:(NSDictionary *) message;
- (void) sendWechatFavoriteRequestWithMessage:(NSDictionary *) message;
- (void) sendWeiboRequestWithMessage:(NSDictionary *) message;
- (void) sendQQShareRequestWithMessage:(NSDictionary *) message;
- (void) sendQZoneShareRequestWithMessage:(NSDictionary *) message;

- (void) sendWechatSessionRequestWithImage:(UIImage *) image;

//pay
- (void) sendWechatPayRequestWithMessage:(NSDictionary *) message completion:(void (^)(NSString *returnKey, NSString *errorMsg)) completion;
- (void) sendAlipayRequestWithOrder:(NSString *) orderStr completion:(void (^)(NSDictionary *resultDic, NSString *errorMsg)) completion;

@end
