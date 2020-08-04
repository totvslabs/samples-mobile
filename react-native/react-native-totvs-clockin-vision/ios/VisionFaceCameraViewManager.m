//
//  VisionModule.m
//  TOTVSClockInVision
//
//  Created by Jansel Rodriguez on 7/27/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(VisionFaceCameraViewManager, RCTViewManager)

    RCT_EXPORT_VIEW_PROPERTY(facing, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(zoom, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(livenessMode, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(livenessBlinkCount, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(overlayGraphicsColor, NSString)
    RCT_EXPORT_VIEW_PROPERTY(isProximityEnabled, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(proximityThreshold, NSNumber)

    RCT_EXPORT_VIEW_PROPERTY(onLiveness, RCTDirectEventBlock)
    RCT_EXPORT_VIEW_PROPERTY(onFaceProximity, RCTDirectEventBlock)
    RCT_EXPORT_VIEW_PROPERTY(onFaceRecognized, RCTDirectEventBlock)

    RCT_EXTERN_METHOD(
        setZoom:(nonnull NSNumber*)zoom
        node:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )
    RCT_EXTERN_METHOD(
        getZoom:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
        enableTorch:(BOOL)enabled
        node:(nonnull NSNumber*)enabled
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )
    RCT_EXTERN_METHOD(
        isTorchEnabled:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
        toggleCamera:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
        setLensFacing:(nonnull NSNumber*)facing
        node:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )
    RCT_EXTERN_METHOD(
        getLensFacing:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )

    RCT_EXTERN_METHOD(
        recognizeStillPicture:(nonnull NSNumber*)saveImage
        node:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )
@end
