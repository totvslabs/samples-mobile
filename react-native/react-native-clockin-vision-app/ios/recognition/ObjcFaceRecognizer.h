//
//  ObjcFaceRecognizer.h
//  ReactNativeTotvsClockinVisionApp
//
//  Created by Jansel Rodriguez on 8/4/20.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 This file is added only for the purpose of bridging the c++ code to objc and objc to swift since
 swift can't access directly c++ code.
 */
@interface ObjcFaceRecognizer : NSObject

-(instancetype)init;

// setup the model with the provided model path where model files are kept
-(void)setup:(NSString *)modelPath;

// trigger the face recognition on the image encoded in base64
-(NSString*) recognizeFaces:(NSString *)imageBase64;

-(void)dealloc;

@end

NS_ASSUME_NONNULL_END
