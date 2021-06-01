//
//  VisionModule.m
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/27/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(VisionModule, NSObject)

    RCT_EXTERN_METHOD(
       setModelOutputDirectoryName: (NSString) name
    )

    RCT_EXTERN_METHOD(
       getModelOutputDirectory: (RCTPromiseResolveBlock)resolve reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
       setupModelDirectories: (RCTPromiseResolveBlock)resolve reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
       trainRecognitionModel: (RCTPromiseResolveBlock)resolve reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
       getImageFileBase64: (NSString) path
       resolve: (RCTPromiseResolveBlock)resolve
       reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
       deleteImageFile: (NSString) path
       resolve: (RCTPromiseResolveBlock)resolve
       reject: (RCTPromiseRejectBlock)reject
    )

@end
