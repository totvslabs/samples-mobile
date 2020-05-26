/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import {
  SafeAreaView,
  StyleSheet,
  View,
  Text,
} from 'react-native';

import CameraView, { Constants } from 'react-native-totvs-camera';

const App = () => {  
  console.log('Constants', { ...Constants });
  
  const rationale = {
    title: 'Title',
    message: 'message',
    buttonPositive: 'OK',
    buttonNegative: 'CANCEL'
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <CameraView style={styles.camera} cameraPermissionOptions={rationale}/>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    justifyContent: 'center'
  },
  camera: {
    width: '100%',
    height: '100%'
  }
});

export default App;
