/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  SafeAreaView,
  TouchableOpacity,
} from 'react-native';

import { VisionModule, FaceCameraConstants, FaceCameraView } from 'react-native-totvs-clockin-vision';

const Constants = FaceCameraConstants;

const onProximity = async proximity => {
  console.log('proximity event:', { ...proximity });
}

const onLiveness = async liveness => {
  console.log('liveness event:', { ...liveness });
}

const onFaceRecognized = async results => {
  console.log('face recognition:', { ...results });
}

const App = () => {
  console.log('Constants', { ...Constants });

  const [cameraView, setCameraView] = useState();  

  // uncomment this if you've copied the model files to the clockin-vision library and want to test 
  // the recognition feature.
  useEffect(() => { 
    const setupModel = async () => {
      await VisionModule.setupModelDirectories()      
      await VisionModule.trainRecognitionModel();
    };
    setupModel();
  }, []);
  
  // types of facing
  const {
    FRONT, BACK
  } = Constants.LENS_FACING;

  // zoom scales
  const {
    MIN, MAX
  } = Constants.ZOOM_LIMITS;

  // liveness modes
  const {
    FACE, EYES, NONE
  } = Constants.LIVENESS_MODE;

  const performAction = async () => {  
    // Vision face actions
    cameraView && cameraView.recognizeStillPicture();
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
        onFaceRecognized={onFaceRecognized}
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
