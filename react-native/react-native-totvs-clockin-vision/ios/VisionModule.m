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
       getModelOutputDirectory: (RCTPromiseResolveBlock)resolve reject: (RCTPromiseRejectBlock)reject
    )

@end
