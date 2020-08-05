
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
