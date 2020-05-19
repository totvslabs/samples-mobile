/**
 * @format
 * @flow strict-local
 */

 
////////////////////////////
// ANCHOR Import Modules
////////////////////////////

import { StyleSheet } from 'react-native';

////////////////////////////
// ANCHOR Styles
////////////////////////////

const cameraView = StyleSheet.create({
  camera: {
    ...StyleSheet.absoluteFill
  }
});

const authorizingView = StyleSheet.create({
  view: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  }
});

const unauthorizedView = StyleSheet.create({
  view: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',    
  },
  text: {
    textAlign: 'center',
    fontSize: 16,    
  }
});

export default {
  cameraView,
  authorizingView,
  unauthorizedView
};