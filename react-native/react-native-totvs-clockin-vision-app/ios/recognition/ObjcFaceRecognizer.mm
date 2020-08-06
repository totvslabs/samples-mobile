//
//  ObjcFaceRecognizer.m
//  ReactNativeTotvsClockinVisionApp
//
//  Created by Jansel Rodriguez on 8/4/20.
//

#import "ObjcFaceRecognizer.h"
#import "FaceRecognizer.h"

/**
  This file is added only for the purpose of bridging the c++ code to objc and objc to swift since
  swift can't access directly c++ code.
*/

@interface ObjcFaceRecognizer ()
  @property(nonatomic) FaceRecognizer* recognizer;
@end

@implementation ObjcFaceRecognizer

- (instancetype)init
{
  self = [super init];
  if (self) {
    self.recognizer = nullptr;
  }
  return self;
}

// setup the model with the provided model path where model files are kept
-(void)setup:(NSString *)modelPath
{
  if (nullptr == _recognizer) {
    char const* path = [modelPath UTF8String];
    
    NSLog(@"path %@", [NSString stringWithUTF8String:path]);
    
//    /var/containers/Bundle/Application/428F4188-B60A-4182-B0DC-1DDF70B25620/ReactNativeTotvsClockinVisionApp.app/ClockInVision.bundle
    
    //char const* path = "/var/mobile/Containers/Data/Application/E1DFC716-A798-4CD3-9198-3CAD49F02416/Documents/carol_offline_face_recognition";
    
    _recognizer = new FaceRecognizer(path);
    _recognizer->loadEmbeddings(path);
    _recognizer->updateThreshold(50);
  }
}

// trigger the face recognition on the image encoded in base64
-(NSString*) recognizeFaces:(NSString *)imageBase64
{
  if (nullptr == _recognizer) {
    @throw([NSException exceptionWithName:@"IllegalStateException" reason:@"You need to call setup before calling this method" userInfo:nil]);
  }
  std::string* base64 = new std::string([imageBase64 UTF8String]);
  std::string  result = _recognizer->faceRecognition(*base64);
  return [NSString stringWithUTF8String:result.c_str()];
}

-(void)dealloc
{
  if (nullptr != _recognizer) {
    delete _recognizer;
    _recognizer = nullptr;
  }
}

@end
