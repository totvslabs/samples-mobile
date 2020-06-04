
# react-native-react-native-totvs-clockin-vision

## Getting started

`$ npm install react-native-react-native-totvs-clockin-vision --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-totvs-clockin-vision`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-react-native-totvs-clockin-vision` and add `RNReactNativeTotvsClockinVision.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReactNativeTotvsClockinVision.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeTotvsClockinVisionPackage;` to the imports at the top of the file
  - Add `new RNReactNativeTotvsClockinVisionPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-totvs-clockin-vision'
  	project(':react-native-react-native-totvs-clockin-vision').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-totvs-clockin-vision/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-totvs-clockin-vision')
  	```


## Usage
```javascript
import RNReactNativeTotvsClockinVision from 'react-native-react-native-totvs-clockin-vision';

// TODO: What to do with the module?
RNReactNativeTotvsClockinVision;
```
  