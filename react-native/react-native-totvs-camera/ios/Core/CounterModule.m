//
//  Counter.m
//  TOTVSCameraKit
//
//  Created by Jansel Rodriguez on 7/1/20.
//  Copyright © 2020 TOTVS Lab. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(CounterModule, NSObject)

    RCT_EXTERN_METHOD(increment)
    RCT_EXTERN_METHOD(getCount: (RCTResponseSenderBlock) sender)
    RCT_EXTERN_METHOD(
        decrement: (RCTPromiseResolveBlock) resolve
        rejecter: ((RCTPromiseResolveBlock)) reject
    )

@end




