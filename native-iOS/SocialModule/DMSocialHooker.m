//
//  Hooker.m
//  HookAppDelegate
//
//  Created by ZhuSheng on 7/8/16.
//  Copyright © 2016 damaiapp. All rights reserved.
//

#import "DMSocialHooker.h"
#import <objc/runtime.h>
#import <UIKit/UIKit.h>
#import "DMSocial.h"

static void beforeApplicationDidFinishLauching(id self, SEL sel, UIApplication *application, NSDictionary *launchOptions);
static BOOL beforeHandleOpenUrl(id self, SEL sel, UIApplication *application, NSURL *url, NSString *sourceApplication, id annotation);

static IMP originFinishLaunching = NULL;
static IMP originHandleOpenUrl = NULL;

typedef BOOL (*Launching)(id self, SEL sel, UIApplication *application, NSDictionary *launchOptions);
typedef BOOL (*HandleOpenUrl)(id self, SEL sel, UIApplication *application, NSURL *url, NSString *sourceApplication, id annotation);

static BOOL replacedDidFinishLaunching(id self, SEL sel, UIApplication *application, NSDictionary *launchOptions)
{
    beforeApplicationDidFinishLauching(self, sel, application, launchOptions);
    
    Launching launch = (Launching)originFinishLaunching;
    return launch(self, sel, application, launchOptions);
}

static BOOL replacedHandleOpenUrl(id self, SEL sel, UIApplication *application, NSURL *url, NSString *sourceApplication, id annotation) {
  BOOL beforeReturnValue = beforeHandleOpenUrl(self, sel, application, url, sourceApplication, annotation);
  HandleOpenUrl handleOpenUrl = (HandleOpenUrl) originHandleOpenUrl;
  return handleOpenUrl ? handleOpenUrl(self, sel, application, url, sourceApplication, annotation) : beforeReturnValue;
}

__attribute__((constructor)) void initBeforeAll(void)
{
  id appDelegateClass = objc_getClass("AppDelegate");
  
  SEL sel = @selector(application:didFinishLaunchingWithOptions:);
  
  Method lauchingMethod = class_getInstanceMethod(appDelegateClass, sel);
  originFinishLaunching = method_setImplementation(lauchingMethod, (IMP)replacedDidFinishLaunching);
  
  SEL handleUrlSel = @selector(application:openURL:sourceApplication:annotation:);
  Method handleUrlMethod = class_getInstanceMethod(appDelegateClass, handleUrlSel);
  if(handleUrlMethod == NULL) {
    class_addMethod(appDelegateClass, handleUrlSel, (IMP)replacedHandleOpenUrl, "B@:@@@@");
  }
  else {
    originHandleOpenUrl = method_setImplementation(handleUrlMethod, (IMP)replacedHandleOpenUrl);
  }
}

static void beforeApplicationDidFinishLauching(id self, SEL sel, UIApplication *application, NSDictionary *launchOptions)
{
  NSLog(@"这里进行进入[AppDelegate application:didFinishLaunchingWithOptions:]之前的处理");
}

static BOOL beforeHandleOpenUrl(id self, SEL sel, UIApplication *application, NSURL *url, NSString *sourceApplication, id annotation) {
  NSLog(@"这里进行[AppDelegate application:openURL:sourceApplication:annotation:]之前的处理");
  [[DMSocial sharedInstance] handleOpenURL:url];
  return YES;
}
