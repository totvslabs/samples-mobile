
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

4. To each of `camera-app/android/totvs-camera-view`, `camera-app/android/totvs-camera-core` and
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