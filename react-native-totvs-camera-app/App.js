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
  console.log(CameraView);

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.camera}>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  },
  camera: {
    width: '100%',
    height: 300,
    backgroundColor: "red"
  }
});

export default App;
