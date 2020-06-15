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
// Import Utility
/////////////////////////////

import { isAbsent, toFiniteFloatOrNull } from '../../utils/numbers';

/////////////////////////////
// Import Components
/////////////////////////////

import FunctionChildren from '../FunctionChildren';
import UnauthorizedView from '../UnauthorizedView';
import PendingAuthorizationView from '../PendingAuthorizationView';


/////////////////////////////
// Import Native Components
/////////////////////////////

const VisionBarcodeCamera = Platform.select({
  ios: View, 
  android: requireNativeComponent('VisionBarcodeCameraView')
});

/////////////////////////////
// Import Native Modules
/////////////////////////////

const VisionBarcodeModule = Platform.select({
  ios: { },
  android: NativeModules.VisionBarcodeModule
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
const BarcodeCameraState = {
  READY: 'READY',
  PENDING: 'PENDING',
  UNAUTHORIZED: 'UNAUTHORIZED'
};
/**
 * Constants exposed by the camera manager of the barcode camera view
 */
export const Constants = {
  // LENS_FACING.FRONT, LENS_FACING.BACK
  LENS_FACING: VisionBarcodeModule.LENS_FACING,
  // ZOOM_LIMITS.MAX, ZOOM_LIMITS.MIN
  ZOOM_LIMITS: VisionBarcodeModule.ZOOM_LIMITS,
  // BARCODE_FORMATS
  BARCODE_FORMAT: VisionBarcodeModule.BARCODE_FORMAT
};



