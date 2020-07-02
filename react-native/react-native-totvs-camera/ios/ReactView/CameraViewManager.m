
#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(CameraViewManager, RCTViewManager)
    RCT_EXPORT_VIEW_PROPERTY(facing, NSNumber)
    RCT_EXPORT_VIEW_PROPERTY(zoom, NSNumber)
//
//    RCT_EXTERN_METHOD(setUpdate:(nonnull NSNumber*)count)
@end
