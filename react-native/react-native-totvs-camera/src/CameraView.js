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
  Platform,
  requireNativeComponent, 
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

import styles from 'react-native-totvs-camera/src/styles';

/////////////////////////////
// Import Native Components
/////////////////////////////

const NativeCamera = requireNativeComponent('CameraView');

/////////////////////////////
// Import Native Modules
/////////////////////////////

// android: CameraModule, ios: CameraViewManager
const CameraModule = NativeModules.CameraModule || NativeModules.CameraViewManager;

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
  onRef?: Function,
  facing?: Number,
  zoom?: Number
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

/**
 * Unlike android, iOS call the property setters on the manager/view even
 * when they are underfined. we default to an appropriate facing.
 */
const DEFAULT_FACING = Constants.LENS_FACING.BACK;

/////////////////////////////
// Utilities
/////////////////////////////

/**
 * return true iff value is either null or undefined 
 * 
 * @param {Object} value 
 */
const isAbsent = value => null === value || undefined == value;

/**
 * Sanitize a value to a pure number or null 
 */
const toFiniteFloatOrNull = value => {
  const isNumeric = value => !isNaN(value) && isFinite(value);
  try { 
    const e = parseFloat(value);
    return isNumeric(e) ? e : null;
  } catch (e) { return null; }
};


/////////////////////////////
// Components
/////////////////////////////

/**
 * Render appropriately a child component based on the type. 
 * We use this in case we need to have a child function component that 
 * need access to the camera component.
 * 
 * Bear in mind that if `children` is a function child, then it must return a 
 * react component.
 */
const Children = ({ camera, ...props }) => {
  const isFunction = e => typeof e === 'function';

  const { children, ...rest } = props;
  
  return isFunction(children) ? children({ camera, ...props }) : children;
}

/**
 * View representing the state of the camera while requesting permission.
 * The caller code can customize this setting `permissionsAskingView` property.
 * 
 * @param {Object} props 
 */
const PermissionsAskingView = () => {
  return (
    <View style={[styles.permissionsAskingView.view]}>
      <ActivityIndicator size="small"/>
    </View>
  );
}

/**
 * View representing the state of the camera when no permission was granted
 * The caller code can customize this setting `permissionsDeniedView` property.
 * 
 * @param {Object} props 
 */
const PermissionsDeniedView = () => {
  const text = "Camera not authorized";
  return (
    <View style={[styles.permissionsDeniedView.view]}>
      <Text style={[styles.permissionsDeniedView.text]}>{text}</Text>
    </View>
  );
}

/**
 * View that display a camera and expose camera-like functionalities.
 * 
 * This view has the capabilty to ask for permissions required to render the camera
 * by itself. If these properties are not set and is disallowed to the camera to
 * ask for permissions, then the caller would be responsible to requests permission
 * required by this underlying component.
 * 
 * This camera component doesn't support permissions settings on iOS platform.
 */
export default class CameraView extends Component<PropsType, StateType> {
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
    onRef: PropTypes.func,
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

  _handle;
  _camera;
  _isMounted;

  constructor(props: PropsType) {
    super(props);    
    // set props default values
    this._isMounted = true;
    // set state initial values
    this.state = { 
      isAuthorized: (Platform.OS == 'ios'), // false otherwise
      isAuthorizationRequested: (Platform.OS == 'ios'), // false otherwise
    };
  }

  /**
   * Set the native view reference
   */
  _setReference = ref => {
    this._camera = ref;
    this._handle = ref && findNodeHandle(ref);
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

    if (Platform.OS == 'ios') {
      expanded.facing = isAbsent(props.facing) ? DEFAULT_FACING : props.facing;      
    }

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
    const result = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.CAMERA, rationale
    );

    if (typeof result === 'boolean') {
      return result;
    } else {
      return PermissionsAndroid.RESULTS.GRANTED === result;
    }
  }

  /**
   * Utilify function to debug the state of the camera.
   * 
   * @param {string} name of the invoking operation
   */
  _onHandle = op => {
    if (!this._handle) {
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
      ? isAuthorized ? CameraState.READY : CameraState.UNAUTHORIZED
      : CameraState.PENDING
      ;
  }
  
  /**
   * Check if the app has camera permissions
   */
  hasCameraPermission = async () => {
    if (Platform.OS == 'ios') {
      throw 'Operation not supported on iOS';
    }

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
    if (Platform.OS == 'ios') {
      throw 'Operation not supported on iOS';
    }

    const { 
      requestPermissions 
    } = this._expandProps(this.props);

    const isAuthorized = requestPermissions && await this.hasCameraPermission();
        
    this._isMounted && this.setState(
      { isAuthorized, isAuthorizationRequested: true }, this._onCameraStateChanged
    );    
  };

  componentDidMount = async () => {
    Platform.OS != 'ios' && await this.refreshCameraState();
    
    // if this view as a delegated `ref` let's rebind the ref to 
    // reflect the fact that now we received the native camera reference
    this.props.onRef && this.props.onRef(this);    
  }
  
  componentWillUnmount = () => {
    this._isMounted = false;
  }

  // Camera Contract

  /**
   * Toggle the camera lens facing
   */
  toggleCamera = async () => {    
    this._onHandle('toggleCamera');

    return this._handle && CameraModule.toggleCamera(this._handle);
  }

  /**
   * Set camera facing. Possible values are one of Constants.LENS_FACING, two possible 
   * values can be passed:
   * 1. Constants.LENS_FACING.BACK
   * 2. Constants.LENS_FACING.FRONT
   */
  setFacing = async facing => {
    this._onHandle('setFacing');

    const {
      FRONT, BACK
    } = Constants.LENS_FACING;

    if (FRONT !== facing && BACK !== facing) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return this._handle && CameraModule.setLensFacing(facing, this._handle);
  }

  /**
   * Returns the current camera facing
   */
  getFacing = async () => {
    this._onHandle('getFacing');

    return this._handle && CameraModule.getLensFacing(this._handle);
  }

  /**
   * Set the camera zoom. possible values are encoded in 
   * [Constants.ZOOM_LIMITS.MIN, Constants.ZOOM_LIMITS.MAX] which are [0.0, 1.0]
   */
  setZoom = async zoom => {
    this._onHandle('setZoom');

    const {
      MIN, MAX
    } = Constants.ZOOM_LIMITS;

    const z = toFiniteFloatOrNull(zoom);

    if (isAbsent(z) || !(MIN <= z && z <= MAX)) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return this._handle && CameraModule.setZoom(z, this._handle);
  }

  /**
   * Returns current zoom
   */
  getZoom = async () => {
    this._onHandle('getZoom');

    return this._handle && CameraModule.getZoom(this._handle);
  }

  /**
   * Enable/Disable the torch (flash light) on this camera
   */
  enableTorch = async enable => {
    this._onHandle('enableTorch');

    return this._handle && CameraModule.enableTorch(enable, this._handle);
  }

  /**
   *  Whether the camera flash/torch is enabled
   */
  isTorchEnabled = async () => {
    this._onHandle('isTorchEnabled');

    return this._handle && CameraModule.isTorchEnabled(this._handle);
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
   * Take a picture and save it in the specified location. The saved 
   * image would be in JPEG format.
   * 
   * If not outputDir is provided then, the image would be saved into the data
   * directory of the app with a random name.
   */
  takePicture = async (outputDir) => {
    this._onHandle('takePicture');

    return this._handle && CameraModule.takePicture(outputDir, this._handle);
  }

  /**
   * View renderization happens here
   */
  render = () => {
    const { style, ...properties } = this._expandProps(this.props);
    
    // if we were authorized or there's a possibly handler function, let's render the camera
    if (this.state.isAuthorized || this.hasFaCC()) {
      return (
        <View style={style}>
          <NativeCamera
            {...properties}
            ref={this._setReference}
            style={styles.cameraView.camera}
          />
          {Children({ camera: this, ...this.props, ...properties })}
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
