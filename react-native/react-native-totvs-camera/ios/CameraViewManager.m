
#import <React/RCTViewManager.h>

/// refs: /// refs: https://teabreak.e-spres-oh.com/swift-in-react-native-the-ultimate-guide-part-1-modules-9bb8d054db03
@interface RCT_EXTERN_MODULE(CameraViewManager, RCTViewManager)

    RCT_EXPORT_VIEW_PROPERTY(facing, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(zoom, NSNumber)

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
        takePicture:(NSString)outputDir
        node:(nonnull NSNumber*)node
        resolve: (RCTPromiseResolveBlock)resolve
        reject: (RCTPromiseRejectBlock)reject
    )
@end
