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
  PermissionsAndroid,
  StyleSheet
} from 'react-native';

import React, { Component } from 'react';

/////////////////////////////
// Import Utility
/////////////////////////////

import { isAbsent, toFiniteFloatOrNull } from '../utils/numbers';
import { type State } from '../utils/types';

/////////////////////////////
// Import Components
/////////////////////////////

import PermissionsDeniedView from './PermissionsDeniedView';
import PermissionsAskingView from './PermissionsAskingView';


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
  requestPermissions?: Boolean,
  permissionsCameraOptions?: Rationale,
  permissionsAskingView?: React.Component,
  permissionsDeniedView?: React.Component,
  onCameraStateChanged?: Function,
  facing?: Number,
  zoom?: Number  
};

type StateType = {
  isAuthorized: bool,
  isAuthorizationRequested: bool
};

/////////////////////////////
// Constants
/////////////////////////////

/**
 * Enum representing camera status
 */
export const BarcodeCameraState = {
  READY: 'READY',
  PENDING: 'PENDING',
  UNAUTHORIZED: 'UNAUTHORIZED'
};
/**
 * Constants exposed by the camera manager of the barcode camera view
 */
export const BarcodeCameraConstants = {
  // LENS_FACING.FRONT, LENS_FACING.BACK
  LENS_FACING: VisionBarcodeModule.LENS_FACING,
  // ZOOM_LIMITS.MAX, ZOOM_LIMITS.MIN
  ZOOM_LIMITS: VisionBarcodeModule.ZOOM_LIMITS,
  // BARCODE_FORMATS
  BARCODE_FORMAT: VisionBarcodeModule.BARCODE_FORMAT
};

/////////////////////////////
// Components
/////////////////////////////


/**
 * View that display a camera and expose camera-like functionalities to detect/recognize
 * barcodes.
 * 
 * This view has the capabilty to ask for permissions required to render the camera
 * by itself. If these properties are not set and is disallowed to the camera to
 * ask for permissions, then the caller would be responsible to requests permission
 * required by this underlying component.
 */
export default class BarcodeCameraView extends Component<PropsType, StateType> {
  /**
   * Properies exposed by this view
   */
  static propTypes = {
    ...ViewPropTypes,
    requestPermissions: PropTypes.bool,
    permissionsCameraOptions: Rationale,
    permissionsAskingView: PropTypes.element,
    permissionsDeniedView: PropTypes.element,
    onCameraStateChanged: PropTypes.func,
    facing: PropTypes.number,
    zoom: PropTypes.number    
  };
  /**
   * Default props for this view
   */
  static defaultProps = {    
    permissionsCameraOptions: { },    
    permissionsAskingView: PermissionsAskingView(),
    permissionsDeniedView: PermissionsDeniedView(),
    onCameraStateChanged: () => { },    
  };

  _cameraHandle;
  _cameraRef;
  _isMounted;

  constructor(props: PropsType) {
    super(props);    
    // set props default values
    this._isMounted = true;
    // set state initial values
    this.state = { 
      isAuthorized: false,
      isAuthorizationRequested: false,      
    };
  }

  /**
   * Set the native view reference
   */
  _setReference = (ref: Object) => {
    this._cameraRef = ref;
    this._cameraHandle  = ref && findNodeHandle(ref);
  }

  /**
   * Utility function to infer the state of other properties to pass down
   * to the native native. e.g if user code set the `onFramePreview` callback
   * property, we might set automatically the property `framePreviewEnabled` to
   * true in case the user didn't set it explicitly.
   */
  _expandProps = ({ children, ...props } : PropsType) => {
    const expanded: PropsType = { ...props };
    
    // enable permission request in case user set permission rationale
    // props and didn't set the allow flag.
    expanded.requestPermissions = (
      props.permissionsCameraOptions
    ) && isAbsent(props.requestPermissions) ? true : props.requestPermissions;

    return expanded;
  }

  /**
   * Notify of camera state changed.
   */
  _onCameraStateChanged = () => {
    this.props.onCameraStateChanged(this.getState())
  }

  /**
   * Selectively request poermissions required to render the native component apropriately.
   */
  _requestCameraPermissions = async (rationale: Rationale) => {
    if (Platform.OS === 'android') {
      const result = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.CAMERA, rationale
      );

      if (typeof result === 'boolean') {
        return result;
      } else {
        return PermissionsAndroid.RESULTS.GRANTED === result;
      }
    }
    return false;
  }

  /**
   * Utilify function to debug the state of the camera.
   * 
   * @param {string} name of the invoking operation
   */
  _checkCameraOp = op => {
    if (!this._cameraHandle) {
      console.warn(`Couldn't perform ${op}. Camera is not initialized yet.`);
    }
  }  

  // public accessors and manipulators

   /**
   * Indicate if this component has a function as a child component. This is used to enable 
   * a handy way to interact with the camera state. e.g we can indicate a function as a child
   * that have an specific way to request permissions and then refresh this component state.
   */
  hasFaCC = () => typeof this.props.children === 'function';

  /**
   * Get the current camera state
   */
  getState = (): State => {
    const {
      isAuthorized, isAuthorizationRequested
    } = this.state;
    
    return isAuthorizationRequested 
      ? isAuthorized ? BarcodeCameraState.READY : BarcodeCameraState.UNAUTHORIZED
      : BarcodeCameraState.PENDING
      ;
  }
  
  /**
   * Check if the app has camera permissions
   */
  hasCameraPermission = async () => {
    const defaults = { 
      title: '', message: '',
      buttonPositive: '',
      buttonNegative: '',
      buttonNeutral: ''
    };
    const rationale = {
      ...defaults,
      ...(this.props.permissionsCameraOptions || { })
    };
    return await this._requestCameraPermissions(rationale);
  }

  refreshCameraState = async () => {
    const { 
      requestPermissions 
    } = this._expandProps(this.props);

    const isAuthorized = requestPermissions && await this.hasCameraPermission();
        
    this._isMounted && this.setState(
      { isAuthorized, isAuthorizationRequested: true }, this._onCameraStateChanged
    );    
  };

  componentDidMount = async () => {
    await this.refreshCameraState();
  }
  
  componentWillUnmount = () => {
    this._isMounted = false;
  }

  // Camera Contract

  /**
   * Toggle the camera lens facing
   */
  toggleCamera = async () => {
    this._checkCameraOp('toggleCamera');

    return this._cameraHandle && VisionBarcodeModule.toggleCamera(this._cameraHandle);
  }

  /**
   * Set camera facing. Possible values are one of BarcodeCameraConstants.LENS_FACING, two possible 
   * values can be passed:
   * 1. BarcodeCameraConstants.LENS_FACING.BACK
   * 2. BarcodeCameraConstants.LENS_FACING.FRONT
   */
  setFacing = async facing => {
    this._checkCameraOp('setFacing');

    const {
      FRONT, BACK
    } = BarcodeCameraConstants.LENS_FACING;

    if (FRONT !== facing && BACK !== facing) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return this._cameraHandle && VisionBarcodeModule.setLensFacing(facing, this._cameraHandle);
  }

  /**
   * Returns the current camera facing
   */
  getFacing = async () => {
    this._checkCameraOp('getFacing');

    return this._cameraHandle && VisionBarcodeModule.getLensFacing(this._cameraHandle);
  }

  /**
   * Set the camera zoom. possible values are encoded in 
   * [BarcodeCameraConstants.ZOOM_LIMITS.MIN, BarcodeCameraConstants.ZOOM_LIMITS.MAX] which are [0.0, 1.0]
   */
  setZoom = async zoom => {
    this._checkCameraOp('setZoom');

    const {
      MIN, MAX
    } = BarcodeCameraConstants.ZOOM_LIMITS;

    const z = toFiniteFloatOrNull(zoom);

    if (isAbsent(z) || !(MIN <= z && z <= MAX)) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return this._cameraHandle && VisionBarcodeModule.setZoom(z, this._cameraHandle);
  }

  /**
   * Returns current zoom
   */
  getZoom = async () => {
    this._checkCameraOp('getZoom');

    return this._cameraHandle && VisionBarcodeModule.getZoom(this._cameraHandle);
  }

  /**
   * Enable/Disable the torch (flash light) on this camera
   */
  enableTorch = async enable => {
    this._checkCameraOp('enableTorch');

    return this._cameraHandle && VisionBarcodeModule.enableTorch(enable, this._cameraHandle);
  }

  /**
   *  Whether the camera flash/torch is enabled
   */
  isTorchEnabled = async () => {
    this._checkCameraOp('isTorchEnabled');

    return this._cameraHandle && VisionBarcodeModule.isTorchEnabled(this._cameraHandle)
  }
  /**
   * Handy function to enable the camera torch. If there's difference between OS to enable the
   * flash light, this function can be modified to selectively enable the flash light 
   * accordingly.
   */
  enableFlash = async enable => this.enableTorch(enable);
  
   /**
    *  Whether the camera flash/torch is enabled
    */
  isFlashEnabled = async () => this.isTorchEnabled(); 

  /**
   * View renderization happens here
   */
  render = () => {
    const { children } = this.props;
    const { style, ...properties } = this._expandProps(this.props);
    
    // if we were authorized or there's a possibly handler function, let's render the camera
    if (this.state.isAuthorized || this.hasFaCC()) {
      return (
        <View style={style}>

          <VisionBarcodeCamera
            {...properties}
            ref={this._setReference}
            style={styles.camera}
          />

          {this.hasFaCC()
            ? children({ camera: this, ...this.props, ...properties })
            : children}
            
        </View>
      );
    }
    // if this component hasn't been authorized to use the camera but requested
    if (!this.state.isAuthorizationRequested) {
      return this.props.permissionsAskingView;
    }
    // otherwises
    return this.props.permissionsDeniedView;
  }
}

////////////////////////////
// ANCHOR Styles
////////////////////////////

const styles = StyleSheet.create({
  camera: {
    ...StyleSheet.absoluteFill
  }
});