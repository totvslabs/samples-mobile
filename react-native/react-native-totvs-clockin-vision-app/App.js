/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, { useState } from 'react';
import {
  StyleSheet,
  View,
  Text,
  SafeAreaView,
  TouchableOpacity,
} from 'react-native';

import { VisionModule, FaceCameraConstants, FaceCameraView } from 'react-native-totvs-clockin-vision';

const Constants = FaceCameraConstants;

const App = () => {
  console.log('Constants', { ...Constants });

  const [cameraView, setCameraView] = useState();
  // types of facing
  const {
    FRONT, BACK
  } = Constants.LENS_FACING;

  // zoom scales
  const {
    MIN, MAX
  } = Constants.ZOOM_LIMITS;

  const performAction = async () => {  
    // VisionModule actions
    const path = await VisionModule.getModelOutputDirectory();
    console.log('vision module model directory: ', { path });

    // Vision face actions
    cameraView && cameraView.toggleCamera();
  };

  /**
   * Example of a FaCC - function as child component of the camera view
   */
  const childFunction = ({ camera }) => {
    console.log(camera.getState());
  }  

  return (
    <SafeAreaView style={styles.safeArea}>

      <FaceCameraView
        onRef={ref => setCameraView(ref)}
        style={styles.camera}
        zoom={MIN}> 
        
        {childFunction} 
      </FaceCameraView>

      <TouchableOpacity style={styles.touchableOpacity} onPress={performAction}>
        <Text style={styles.actionButtonText}>{"Perform Action"}</Text>
      </TouchableOpacity>

    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor: 'black'
  },
  camera: {
    width: '100%',
    height: '100%',
  },
  touchableOpacity: {
    position: 'absolute',
    bottom: 36,
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
