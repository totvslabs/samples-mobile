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

import CameraView from 'react-native-totvs-camera';

const App = () => {  
  return (
    <SafeAreaView style={styles.safeArea}>
      <CameraView style={styles.camera}/>
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
