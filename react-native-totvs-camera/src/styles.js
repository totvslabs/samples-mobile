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

const pendingAuthorizationView = StyleSheet.create({
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
    color: 'black'
  }
});

export default {
  cameraView,
  pendingAuthorizationView,
  unauthorizedView
};