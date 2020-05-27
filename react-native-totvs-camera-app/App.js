/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, { useState } from 'react';
import {
  SafeAreaView,
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
} from 'react-native';

import CameraView, { Constants } from 'react-native-totvs-camera';

const App = () => {  
  console.log('Constants', { ...Constants });

  const [cameraView, setCameraView] = useState();
  
  const rationale = {
    title: 'Title',
    message: 'message',
    buttonPositive: 'OK',
    buttonNegative: 'CANCEL'
  };

  // types of facing
  const {
    FRONT, BACK
  } = Constants.LENS_FACING;

  // zoom scales
  const {
    MIN, MAX
  } = Constants.ZOOM_LIMITS;

  // perform the action for the button. You can change the action
  // to test different operations.
  const performAction = () => {
    // cameraView && cameraView.setFacing(BACK);
    // cameraView && cameraView.setZoom(0.5);
    // cameraView && cameraView.toggleCamera()
    // cameraView && cameraView.enableFlash(false);
    cameraView && cameraView.enableTorch(false); // synonyms for the previous
  };


  /**
   * Example of a FaCC - function as child component of the camera view
   */
  const childFunction = ({ camera }) => {
    console.log(camera.getState());
  }

  return (
    <SafeAreaView style={styles.safeArea}>

      <CameraView
        ref={ref => setCameraView(ref)}
        style={styles.camera}
        zoom={MIN}
        cameraPermissionOptions={rationale}> 
        {childFunction} 
      </CameraView>

      <TouchableOpacity style={styles.touchableOpacity} onPress={performAction}>
        <Text style={styles.actionButtonText}>{"Torch/Flash"}</Text>
      </TouchableOpacity>

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
  },
  touchableOpacity: {
    position: 'absolute',
    bottom: 16,
    backgroundColor: 'white',
    width: 200,
    height: 48,
    borderRadius: 8,
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    alignSelf: 'center'
  },
  actionButtonText: {
    color: 'grey',
    fontSize: 16,
    fontWeight: 'bold'
  }
});

export default App;
