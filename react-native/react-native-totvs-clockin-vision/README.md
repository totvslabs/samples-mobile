
### Manual installation

Additional steps are required if this library is manually installed in a project node_modules
directory.

Here we describe the steps with a hypothetical react-native project called `camera-app`.

#### Android

Because `react-native-totvs-clockin-vision` depends also on `totvs-camera-core`,`totvs-camera-view`
and `totvs-camera-vision` we need to also configure manually these dependencies when we manually
install this library.

1. Copy this library into `camera-app/node_modules`
2. Copy libraries `totvs-camera-core`, `totvs-camera-view` and `totvs-camera-vision`
   into `camera-app/android` directory.
3. To the file `camera-app/android/settings.gradle` append the next lines:
```javascript
include ':totvs-camera-core'
include ':totvs-camera-view'
include ':totvs-camera-vision'
project(':totvs-camera-core').projectDir = new File(rootProject.projectDir, './totvs-camera-core')
project(':totvs-camera-view').projectDir = new File(rootProject.projectDir, './totvs-camera-view')
project(':totvs-camera-vision').projectDir = new File(rootProject.projectDir, './totvs-camera-vision')
```

This step will map any reference to the dependencies of the libraries to the physical location
of the libraries we already copied.

4. (If the lines are not already there) To each of `camera-app/android/totvs-camera-view`, `camera-app/android/totvs-camera-core` and
    `camera-app/android/totvs-camera-vision` append to their `build.gradle` file, the following lines:

```javascript
buildscript {
  ext {
    kotlin_version = '1.3.72'
  }
  repositories {
    google()
    jcenter()
   }
  dependencies {
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
  }
}

repositories {
  google()
  jcenter()
}
```

5. Done


#### iOS

This library depends on `TOTVSCameraKit/View` pod. in order to install properly this library into a 
react-native application we can do manual linking following the next steps:

1. Copy this library into `camera-app/node_modules`
2. Open `camera-app/ios/camera-app.xcworspace`. Do notice that this is the `.xcworkspace` file not the project
  file. We require this in order to work with the loaded pods, also, because once our libraries are load to the `Pods.xcodeproj` we'll be able to modify our libraries source code.
3. open `camera-app/ios/Podfile` and add:
```javascript
  // must be imported in order react-native-totvs-clockin-vision dependencies be resolved. we can remove this when we 
  // publish TOTVSCameraKit
  pod 'TOTVSCameraKit', :path => '../../../ios/TOTVSCameraKit'
  // or
  pod 'TOTVSCameraKit/Vision', :path => '../../../ios/TOTVSCameraKit'
```
You need to replace `../../../ios/TOTVSCameraKit` by the appropriate location of these libraries in your PC.

4. Inside `camera-app/ios` run `pod install`
5. Done


Also note that this is the approach recomended when a need to modify this or any submodule of `TOTVSCameraKit` libraries. You'll find the project `TOTVSCameraKit` and `react-native-totvs-clockin-vision` in the `Pods.xcodeproj` of `camera-app/ios`. Modify source code from these projects.


### Miscellaneous

#### iOS

The native c++ library used for recognitions tasks doesn't offer by itself an interface for swift code to interact with it
in order to use this library even in production code, some other steps are required:

1. An `objc` bridge is needed to expose c++ code to swift by exposing the `objc` code itself. This will allow us to expose 
`FaceRecognizer.h` interface to swift code. We achieve that by creating `ObjcFaceRecognizer.h` and `ObjcFaceRecognizer.mm`.
These files are already developed and ready for reuse. You can find then under the sample project 
`react-native-totvs-clockin-vision-app/ios` under `recognition` folder. Just copy then onto the new swift project that uses
the c++ recognition library.
2. In order to interact with the `react-native-totvs-clockin-vision` library, a model is required to be injected to the
library. Under the project `react-native-totvs-clockin-vision-app/ios` there's already a ready to use model class that you 
reuse on new projects that uses both, the c++ recognition library and the `react-native-totvs-clockin-vision` library.
The required files are `NativeRecognitionModel.swift` and `NativeFace.swift`.
3. You'll need to inject `NativeRecognitionModel` to the `react-native-totvs-clockin-vision`, you'll see how to do that
under `AppDelegate.m` file.


#### Platform Implementation Differences

As of right now, android and iOS implements a couple of distinct features that ar especific to each platfom until asked otherwise, here's a list:

1. `android(only)` Face detection as part of still picture recognition process. This detection is done to extract information about the face and decide whether or not the native recognition model will skip or not a detection phase.
2. `android(only)` Proximity detection based on the face location. This feature allows to compute the detected face position and determine whether is inside the visible camera view frame or not.
