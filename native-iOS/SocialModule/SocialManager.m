//
//  SocialManager.m
//  Yml
//
//  Created by ZhuSheng on 01/11/2016.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "SocialManager.h"
#import "DMSocial.h"

@implementation SocialManager

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(configure:(NSDictionary *) configuration) {
  NSString *weiboAppKey = [configuration objectForKey:@"weiboAppKey"];
  NSString *qqAppId = [configuration objectForKey:@"qqAppId"];
  NSString *wechatAppId = [configuration objectForKey:@"wechatAppId"];
  NSString *alipayScheme = [configuration objectForKey:@"alipayScheme"];
  
  NSDictionary *configInfo = @{DMSocialQQAppIdConfigurationKey: qqAppId ? qqAppId : @"",
                               DMSocialWeiboAppKeyConfigurationKey: weiboAppKey ? weiboAppKey : @"",
                               DMSocialWechatAppIdConfigurationKey: wechatAppId ? wechatAppId : @"",
                               DMSocialAlipaySchemeConfigurationKey: alipayScheme ? alipayScheme : @""
                                 };
  
  [[DMSocial sharedInstance] setConfigInfo:configInfo];
}

RCT_EXPORT_METHOD(authorizeByWechat:(RCTResponseSenderBlock) callback) {
  [[DMSocial sharedInstance] sendWechatAuthorizeRequestWithViewController:nil completion:^(NSString *wxCode) {
    if (callback) {
      callback(@[[NSNull null], wxCode]);
    }
  }];
}

RCT_EXPORT_METHOD(authorizeByWeibo:(RCTResponseSenderBlock) callback) {
  [[DMSocial sharedInstance] sendWeiboAuthorizeRequestCompletion:^(NSDictionary *info) {
    if (callback) {
      callback(@[[NSNull null], info]);
    }
  }];
}

RCT_EXPORT_METHOD(authorizeByQQ:(RCTResponseSenderBlock) callback) {
  [[DMSocial sharedInstance] sendQQAuthorizeRequestCompletion:^(NSDictionary *info) {
    if (callback) {
      callback(@[[NSNull null], info]);
    }
  }];
}

RCT_EXPORT_METHOD(checkWechatInstallation:(RCTResponseSenderBlock) callback) {
  if (callback) {
    callback(@[[NSNull null], @([[DMSocial sharedInstance] isWechatInstalled])]);
  }
}

RCT_EXPORT_METHOD(checkWeiboInstallation:(RCTResponseSenderBlock) callback) {
  if (callback) {
    callback(@[[NSNull null], @([[DMSocial sharedInstance] isWeiboInstalled])]);
  }
}

RCT_EXPORT_METHOD(checkQQInstallation:(RCTResponseSenderBlock) callback) {
  if (callback) {
    callback(@[[NSNull null], @([[DMSocial sharedInstance] isQQinstalled])]);
  }
}

RCT_EXPORT_METHOD(payByWechat:(NSDictionary *) info whenCompleted:(RCTResponseSenderBlock) callback) {
  [[DMSocial sharedInstance] sendWechatPayRequestWithMessage:info completion:^(NSString *returnKey, NSString *errorMsg) {
    if (errorMsg) {
      if (callback) {
        callback(@[@{@"message": errorMsg}, [NSNull null]]);
      }
    }
    else {
      if (callback) {
        callback(@[@{@"message": @"支付成功"}, returnKey]);
      }
    }
  }];
}

RCT_EXPORT_METHOD(payByAlipay:(NSDictionary *) info whenCompleted:(RCTResponseSenderBlock) callback) {
  NSString *orderStr = info[@"order"];
  [[DMSocial sharedInstance] sendAlipayRequestWithOrder:orderStr completion:^(NSDictionary *resultDic, NSString *errorMsg) {
    if (errorMsg) {
      if (callback) {
        callback(@[@{@"message": errorMsg}, [NSNull null]]);
      }
    }
    else {
      if (callback) {
        callback(@[@{@"message": @"支付成功"}, resultDic]);
      }
    }
  }];
}

RCT_EXPORT_METHOD(shareToWechatSession:(NSDictionary *) info) {
  NSMutableDictionary *message = [info mutableCopy];
  
  NSString *thumbnailUrl = info[@"thumbnail"];
  if (thumbnailUrl) {
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:[thumbnailUrl hasPrefix:@"/"] ? [NSURL fileURLWithPath:thumbnailUrl] : [NSURL URLWithString:thumbnailUrl]];
    [message setObject:[[UIImage alloc] initWithData:imageData] forKey:@"image"];
  }
  
  [[DMSocial sharedInstance] sendWechatSessionRequestWithMessage:message];
}

RCT_EXPORT_METHOD(shareToWechatTimeline:(NSDictionary *) info) {
  NSMutableDictionary *message = [info mutableCopy];
  
  NSString *thumbnailUrl = info[@"thumbnail"];
  if (thumbnailUrl) {
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:[thumbnailUrl hasPrefix:@"/"] ? [NSURL fileURLWithPath:thumbnailUrl] : [NSURL URLWithString:thumbnailUrl]];
    [message setObject:[[UIImage alloc] initWithData:imageData] forKey:@"image"];
  }
  
  [[DMSocial sharedInstance] sendWechatTimelineRequestWithMessage:message];
}

RCT_EXPORT_METHOD(shareToWechatFavorite:(NSDictionary *) info) {
  NSMutableDictionary *message = [info mutableCopy];
  
  NSString *thumbnailUrl = info[@"thumbnail"];
  if (thumbnailUrl) {
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:[thumbnailUrl hasPrefix:@"/"] ? [NSURL fileURLWithPath:thumbnailUrl] : [NSURL URLWithString:thumbnailUrl]];
    [message setObject:[[UIImage alloc] initWithData:imageData] forKey:@"image"];
  }
  
  [[DMSocial sharedInstance] sendWechatFavoriteRequestWithMessage:message];
}

RCT_EXPORT_METHOD(shareToWeibo:(NSDictionary *) info) {
  NSMutableDictionary *message = [info mutableCopy];
  
  NSString *thumbnailUrl = info[@"thumbnail"];
  if (thumbnailUrl) {
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:[thumbnailUrl hasPrefix:@"/"] ? [NSURL fileURLWithPath:thumbnailUrl] : [NSURL URLWithString:thumbnailUrl]];
    [message setObject:[[UIImage alloc] initWithData:imageData] forKey:@"image"];
  }
  
  dispatch_async(dispatch_get_main_queue(), ^{
    [[DMSocial sharedInstance] sendWeiboRequestWithMessage:message];
  });
  
}

RCT_EXPORT_METHOD(shareToQQ:(NSDictionary *) info) {
  NSMutableDictionary *message = [info mutableCopy];
  
  NSString *thumbnailUrl = info[@"thumbnail"];
  if (thumbnailUrl) {
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:[thumbnailUrl hasPrefix:@"/"] ? [NSURL fileURLWithPath:thumbnailUrl] : [NSURL URLWithString:thumbnailUrl]];
    [message setObject:[[UIImage alloc] initWithData:imageData] forKey:@"image"];
  }
  
  dispatch_async(dispatch_get_main_queue(), ^{
    [[DMSocial sharedInstance] sendQQShareRequestWithMessage:message];
  });
  
}

RCT_EXPORT_METHOD(shareToQZone:(NSDictionary *) info) {
  NSMutableDictionary *message = [info mutableCopy];
  
  NSString *thumbnailUrl = info[@"thumbnail"];
  if (thumbnailUrl) {
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:[thumbnailUrl hasPrefix:@"/"] ? [NSURL fileURLWithPath:thumbnailUrl] : [NSURL URLWithString:thumbnailUrl]];
    [message setObject:[[UIImage alloc] initWithData:imageData] forKey:@"image"];
  }
  
  dispatch_async(dispatch_get_main_queue(), ^{
    [[DMSocial sharedInstance] sendQZoneShareRequestWithMessage:message];
  });
  
}

@end
