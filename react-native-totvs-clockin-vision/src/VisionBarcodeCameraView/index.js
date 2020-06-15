/**
 * @format
 * @flow strict-local
 */

/////////////////////////////
// Imports
////////////////////////////
import PropTypes from 'prop-types';

import {
  findNodeHandle,  
  NativeModules,
  requireNativeComponent, 
  Platform,
  ViewPropTypes,
  View,
  ActivityIndicator,
  Text,  
  PermissionsAndroid
} from 'react-native';

import React, { Component } from 'react';

/////////////////////////////
// Import Styles
/////////////////////////////

import styles from './styles';

/////////////////////////////
// Import Native Components
/////////////////////////////

const VisionFaceCamera = Platform.select({
  ios: View, 
  android: requireNativeComponent('VisionFaceCameraView')
});

/////////////////////////////
// Import Native Modules
/////////////////////////////

const VisionFaceModule = Platform.select({
  ios: { },
  android: NativeModules.VisionFaceModule
});

/////////////////////////////
// Type System
////////////////////////////

const Rationale = PropTypes.shape({
  title: PropTypes.string.isRequired, 
  message: PropTypes.string.isRequired, 
  buttonPositive: PropTypes.string, 
  buttonNegative: PropTypes.string,
  buttonNeutral: PropTypes.string
});

type PropsType = typeof View.props & {
  requestPermissions?: boolean,
  cameraPermissionOptions?: Rationale,
  pendingAuthorizationView?: React.Component,
  unauthorizedView?: React.Component,
  onCameraStateChanged?: Function
};

type StateType = {
  isAuthorized: bool,
  isAuthorizationRequested: bool
};

export type State = 'READY' | 'PENDING' | 'UNAUTHORIZED';

/////////////////////////////
// Constants
/////////////////////////////

/**
 * Enum representing camera status
 */
const CameraState = {
  READY: 'READY',
  PENDING: 'PENDING',
  UNAUTHORIZED: 'UNAUTHORIZED'
};
/**
 * Constants exposed by the camera manager of the camera view
 */
export const Constants = {
  // LENS_FACING.FRONT, LENS_FACING.BACK
  LENS_FACING: CameraModule.LENS_FACING,
  // ZOOM_LIMITS.MAX, ZOOM_LIMITS.MIN
  ZOOM_LIMITS: CameraModule.ZOOM_LIMITS,
};