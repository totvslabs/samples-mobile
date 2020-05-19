
# react-native-totvs-camera

## Getting started

`$ npm install react-native-totvs-camera --save`

### Mostly automatic installation

`$ react-native link react-native-totvs-camera`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeTotvsCameraPackage;` to the imports at the top of the file
  - Add `new RNReactNativeTotvsCameraPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-totvs-camera'
  	project(':react-native-totvs-camera').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-totvs-camera/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-totvs-camera')
  	```


## Usage
```javascript
import TotvsCamera from 'react-native-totvs-camera';
```
  