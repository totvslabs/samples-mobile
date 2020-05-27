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

const NativeCamera = Platform.select({
  ios: View, 
  android: requireNativeComponent('CameraView')
});

/////////////////////////////
// Import Native Modules
/////////////////////////////

const CameraModule = Platform.select({
  ios: { },
  android: NativeModules.CameraModule
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
 * The caller code can customize this setting `pendingAuthorizationView` property.
 * 
 * @param {Object} props 
 */
const PendingAuthorizationView = () => {
  return (
    <View style={[styles.pendingAuthorizationView.view]}>
      <ActivityIndicator size="small"/>
    </View>
  );
}

/**
 * View representing the state of the camera when no permission was granted
 * The caller code can customize this setting `unathorizedView` property.
 * 
 * @param {Object} props 
 */
const UnauthorizedView = () => {
  const text = "Camera not authorized";
  return (
    <View style={[styles.unauthorizedView.view]}>
      <Text style={[styles.unauthorizedView.text]}>{text}</Text>
    </View>
  );
}

/**
 * View that bridge the native implementation of the camera to JS.
 */
export default class CameraView extends Component<PropsType, StateType> {
  /**
   * Properies exposed by this view
   */
  static propTypes = {
    ...ViewPropTypes,
    requestPermissions: PropTypes.bool,
    cameraPermissionOptions: Rationale,
    pendingAuthorizationView: PropTypes.element,
    unauthorizedView: PropTypes.element,
    onCameraStateChanged: PropTypes.func
  };
  /**
   * Default props for this view
   */
  static defaultProps = {    
    cameraPermissionOptions: { },    
    pendingAuthorizationView: PendingAuthorizationView(),
    unauthorizedView: UnauthorizedView(),
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
      props.cameraPermissionOptions
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
    const defaults = { 
      title: '', message: '',
      buttonPositive: '',
      buttonNegative: '',
      buttonNeutral: ''
    };
    const rationale = {
      ...defaults,
      ...(this.props.cameraPermissionOptions || { })
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
  toggleCamera = async () => CameraModule.toggleCamera(this._cameraHandle);

  /**
   * Set camera facing. Possible values are one of Constants.LENS_FACING, two possible 
   * values can be passed:
   * 1. Constants.LENS_FACING.BACK
   * 2. Constants.LENS_FACING.FRONT
   */
  setFacing = async facing => {
    const {
      FRONT, BACK
    } = Constants.LENS_FACING;

    if (FRONT !== facing && BACK !== facing) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return CameraModule.setLensFacing(facing, this._cameraHandle);
  }

  /**
   * Set the camera zoom. possible values are encoded in 
   * [Constants.ZOOM_LIMITS.MIN, Constants.ZOOM_LIMITS.MAX] which are [0.0, 1.0]
   */
  setZoom = async zoom => {
    const {
      MIN, MAX
    } = Constants.ZOOM_LIMITS;

    const z = toFiniteFloatOrNull(zoom);

    if (!isAbsent(z) && !(MIN <= z && z <= MAX)) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return CameraModule.setZoom(z, this._cameraHandle);
  }

  /**
   * Enable/Disable the torch (flash light) on this camera
   */
  enableTorch = async enable => CameraModule.enableTorch(enable, this._cameraHandle);

  /**
   * Handy function to enable the camera torch. If there's difference between OS to enable the
   * flash light, this function can be modified to selectively enable the flash light 
   * accordingly.
   */
  enableFlash = async enable => this.enableTorch(enable);
  

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
          {Children({ camera: this, ...this.props })}
        </View>
      );
    }
    // if this component hasn't been authorized to use the camera but requested
    if (!this.state.isAuthorizationRequested) {
      return this.props.pendingAuthorizationView;
    }
    // otherwise
    return this.props.unauthorizedView;
  }
}

