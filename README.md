
## Android Projects

### deep-linking

Native android project to showcase the deep-linking capabilities of clock-in apps.

### totvs-camera-core

Native android library tha contains the bases for camera-capable libraries. This library is used
in `totvs-camera-view` and `totvs-camera-vision`

### totvs-camera-view

Native android library that offer a camera view, a graphic overlay to draw on top of the camera
and an analyzer interface to analyze camera preview frames.

This project depends on `totvs-camera-core`

### totvs-camera-vision

Native android library that offers vision capabilities, e.g barcode detection, object detection,
face detection.

This project offers an analyzer that act as a detector manager and expose an stream of detections
that projects can use to perform specific tasks.

This project depends on `totvs-camera-core`

### totvs-camera-app

Native android app that uses `totvs-camera-core`, `totvs-camera-view` and `totvs-camera-vision`
to showcase the potential of integration between these three modules, to perform camera-like
and detection-like functionalities.

This project depends on `totvs-camera-core`, `totvs-camera-view` and `totvs-camera-vision`

## React Native Projects

### react-native-totvs-camera

React Native library that expose `totvs-camera-view` functionality to react-native apps.

This project depends on `totvs-camera-core` and `totvs-camera-view`

### react-native-totvs-camera-app

React Native app that uses `react-native-totvs-camera`

### react-native-totvs-clockin-vision

React Native library that uses `totvs-camera-core`, `totvs-camera-view` and `totvs-camera-vision`
to expose a react-native interface for functionalities related to clock-in apps.

This project showcase capabilities like: Liveness, Proximity, Recognition.

## iOS Projects