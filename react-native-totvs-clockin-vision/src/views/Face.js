/**
 * @format
 * @flow strict-local
 */

/////////////////////////////
// Imports
////////////////////////////
import PropTypes from 'prop-types';
import React, { Component } from 'react';
import {
  findNodeHandle,
  NativeModules,
  PermissionsAndroid, 
  Platform, 
  requireNativeComponent,
  StyleSheet, 
  View, 
  ViewPropTypes
} from 'react-native';
/////////////////////////////
// Import Utility
/////////////////////////////
import { isAbsent, toFiniteFloatOrNull } from '../utils/numbers';
import { State } from '../utils/types';
import PermissionsAskingView from './PermissionsAskingView';
/////////////////////////////
// Import Components
/////////////////////////////
import PermissionsDeniedView from './PermissionsDeniedView';

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
  requestPermissions?: Boolean,
  permissionsCameraOptions?: Rationale,
  permissionsAskingView?: React.Component,
  permissionsDeniedView?: React.Component,
  onCameraStateChanged?: Function,
  onRef?: Function,
  facing?: Number,
  zoom?: Number,  
  livenessMode?: Number,
  livenessBlinkCount?: Number,
  isProximityEnabled?: Boolean,
  proximityThreshold?: Number,
  overlayGraphicsColor?: string,
  onLiveness?: Function,
  onFaceProximity?: Function,
  onFaceRecognized?: Function,
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
export const FaceCameraState = {
  READY: 'READY',
  PENDING: 'PENDING',
  UNAUTHORIZED: 'UNAUTHORIZED'
};
/**
 * Constants exposed by the camera manager of the face camera view
 */
export const FaceCameraConstants = {
  // LENS_FACING.FRONT, LENS_FACING.BACK
  LENS_FACING: VisionFaceModule.LENS_FACING,
  // ZOOM_LIMITS.MAX, ZOOM_LIMITS.MIN
  ZOOM_LIMITS: VisionFaceModule.ZOOM_LIMITS,
  // LIVENESS_MODE
  LIVENESS_MODE: VisionFaceModule.LIVENESS_MODE
};

/////////////////////////////
// Components
/////////////////////////////


/**
 * View that display a camera and expose camera-like functionalities to detect/recognize
 * faces.
 * 
 * This view has the capabilty to ask for permissions required to render the camera
 * by itself. If these properties are not set and is disallowed to the camera to
 * ask for permissions, then the caller would be responsible to requests permission
 * required by this underlying component.
 */
export default class FaceCameraView extends Component<PropsType, StateType> {
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
    zoom: PropTypes.number,
    livenessMode: PropTypes.number,
    livenessBlinkCount: PropTypes.number,
    isProximityEnabled: PropTypes.bool,
    proximityThreshold: PropTypes.number,
    overlayGraphicsColor: PropTypes.string,
    onLiveness: PropTypes.func,
    onFaceProximity: PropTypes.func,
    onFaceRecognized: PropTypes.func,
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
      isAuthorized: false,
      isAuthorizationRequested: false,      
    };
  }

  /**
   * Set the native view reference
   */
  _setReference = (ref: Object) => {
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

    // enable proximity in case user set proximity threshold
    // props and didn't set the allow flag.
    expanded.isProximityEnabled = (
      props.proximityThreshold
    ) && isAbsent(props.isProximityEnabled) ? true : props.isProximityEnabled;
    
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
  _onHandle = op => {
    if (!this._handle) {
      console.warn(`Couldn't perform ${op}. Camera is not initialized yet.`);
    }
  }

  /**
   * Called when isProximityEnabled is true.
   */
  _onFaceProximity = event => {
    const payload = event.nativeEvent;
    
    const { 
      onFaceProximity
    } = this.props;

    onFaceProximity && onFaceProximity(payload);
  }

  /**
   * Called when we set a valid {FaceCameraConstants.LIVENESS_MODE} distinct from NONE
   */
  _onLiveness = event => {
    const payload = event.nativeEvent;
    
    const { 
      onLiveness
    } = this.props;

    onLiveness && onLiveness(payload);
  }

  /**
   * Called when we trigger {recognizeStillPicture} 
   */
  _onFaceRecognized = event => {
    const payload = event.nativeEvent;

    const {
      onFaceRecognized
    } = this.props;

    onFaceRecognized && onFaceRecognized(payload);
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
      ? isAuthorized ? FaceCameraState.READY : FaceCameraState.UNAUTHORIZED
      : FaceCameraState.PENDING
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

    return this._handle && VisionFaceModule.toggleCamera(this._handle);
  }

  /**
   * Set camera facing. Possible values are one of FaceCameraConstants.LENS_FACING, two possible 
   * values can be passed:
   * 1. FaceCameraConstants.LENS_FACING.BACK
   * 2. FaceCameraConstants.LENS_FACING.FRONT
   */
  setFacing = async facing => {
    this._onHandle('setFacing');

    const {
      FRONT, BACK
    } = FaceCameraConstants.LENS_FACING;

    if (FRONT !== facing && BACK !== facing) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return this._handle && VisionFaceModule.setLensFacing(facing, this._handle);
  }

  /**
   * Returns the current camera facing
   */
  getFacing = async () => {
    this._onHandle('getFacing');

    return this._handle && VisionFaceModule.getLensFacing(this._handle);
  }

  /**
   * Set the camera zoom. possible values are encoded in 
   * [FaceCameraConstants.ZOOM_LIMITS.MIN, FaceCameraConstants.ZOOM_LIMITS.MAX] which are [0.0, 1.0]
   */
  setZoom = async zoom => {
    this._onHandle('setZoom');

    const {
      MIN, MAX
    } = FaceCameraConstants.ZOOM_LIMITS;

    const z = toFiniteFloatOrNull(zoom);

    if (isAbsent(z) || !(MIN <= z && z <= MAX)) {
      return console.warn(`Invalid facing value ${facing} possible values are front=${FRONT}, back=${BACK}`);
    }

    return this._handle && VisionFaceModule.setZoom(z, this._handle);
  }

  /**
   * Returns current zoom
   */
  getZoom = async () => {
    this._onHandle('getZoom');

    return this._handle && VisionFaceModule.getZoom(this._handle);
  }

  /**
   * Enable/Disable the torch (flash light) on this camera
   */
  enableTorch = async enable => {
    this._onHandle('enableTorch');

    return this._handle && VisionFaceModule.enableTorch(enable, this._handle);
  }

  /**
   *  Whether the camera flash/torch is enabled
   */
  isTorchEnabled = async () => {
    this._onHandle('isTorchEnabled');

    return this._handle && VisionFaceModule.isTorchEnabled(this._handle)
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
   * Trigger the recognition on an still picture. If {saveImage} is true, then the result will
   * contain a path for the saved image.
   * Results of this method are obtained through the dispatch of the [OnFaceRecognized] event
   */
  recognizeStillPicture = async saveImage => {
    this._onHandle('recognizeStillPicture');

    return this._handle && VisionFaceModule.recognizeStillPicture(saveImage || false, this._handle);
  }

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

          <VisionFaceCamera
            {...properties}
            style={styles.camera}
            onLiveness={this._onLiveness}
            onFaceProximity={this._onFaceProximity}            
            onFaceRecognized={this._onFaceRecognized}
            ref={this._setReference}     
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