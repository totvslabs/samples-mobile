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
  UIManager,
  requireNativeComponent, 
  ViewPropTypes,
  View, 
} from 'react-native';

import React, { Component } from 'react';

/////////////////////////////
// Import Styles
/////////////////////////////

import styles from './styles';

/////////////////////////////
// Import Native Components
/////////////////////////////
const COMPONENT_NAME = "CameraView"

const NativeCamera = requireNativeComponent(COMPONENT_NAME)

/////////////////////////////
// Import Native Modules
/////////////////////////////
const {
  /**
   * On iOS there's no module exposed only the ViewManager, but unlike with the 
   * android counterpart we don't perform operations directly neither on the 
   * manager not in the module(non-existent on iOS) we call manager operations 
   * through dispatchViewManagerCommand
   */  
  Manager: CameraModule,
  /**
   * Exported Constants from native view manager
   */
  Constants: NativeConstants,
  /**
   * Available operations exposed by the native view manager
   */
  Commands
} = UIManager[COMPONENT_NAME];

/////////////////////////////
// Type System
////////////////////////////

type PropsType = typeof View.props & {
  onCameraStateChanged?: Function,
  onRef?: Function,
  facing?: Number,
  zoom?: Number
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
  LENS_FACING: NativeConstants.LENS_FACING,
  // ZOOM_LIMITS.MAX, ZOOM_LIMITS.MIN
  ZOOM_LIMITS: NativeConstants.ZOOM_LIMITS,
};

/**
 * Unlike android, iOS call the property setters on the manager/view even
 * when they are underfined. we default to an appropriate facing.
 */
const DEFAULT_FACING = NativeConstants.LENS_FACING.BACK;


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
 * View that display a camera and expose camera-like functionalities.
 * 
 * This view has the capabilty to ask for permissions required to render the camera
 * by itself. If these properties are not set and is disallowed to the camera to
 * ask for permissions, then the caller would be responsible to requests permission
 * required by this underlying component.
 */
export default class CameraView extends Component<PropsType, StateType> {
  /**
   * Properies exposed by this view
   */
  static propTypes = {
    ...ViewPropTypes,
    onCameraStateChanged: PropTypes.func,
    onRef: PropTypes.func,
    facing: PropTypes.number,
    zoom: PropTypes.number
  };
  /**
   * Default props for this view
   */
  static defaultProps = {
    onCameraStateChanged: () => { },
  };

  _handle;
  _camera;
  _isMounted;

  constructor(props: PropsType) {
    super(props);    
    // set props default values
    this._isMounted = true;
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
  
    expanded.facing = isAbsent(props.facing) ? DEFAULT_FACING : props.facing;

    return expanded;
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

  _dispatch = async (command, ...args) => {
    return this._handle && UIManager.dispatchViewManagerCommand(
      this._handle,
      command, 
      ...args
    );
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
  getState = (): State => CameraState.READY;
    
  componentDidMount = async () => {  
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

    // return this._handle && CameraModule.toggleCamera(this._handle);
    return true;
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

    // return this._handle && CameraModule.setLensFacing(facing, this._handle);
    return 
  }

  /**
   * Returns the current camera facing
   */
  getFacing = async () => {
    this._onHandle('getFacing');

    // return this._handle && CameraModule.getLensFacing(this._handle);
    return true;
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

    // return this._handle && CameraModule.setZoom(z, this._handle);
    return true;
  }

  /**
   * Returns current zoom
   */
  getZoom = async () => {
    this._onHandle('getZoom');

    // return this._handle && CameraModule.getZoom(this._handle);
    return true;
  }

  /**
   * Enable/Disable the torch (flash light) on this camera
   */
  enableTorch = async enable => {
    this._onHandle('enableTorch');

    // return this._handle && CameraModule.enableTorch(enable, this._handle);
    return true;
  }

  /**
   *  Whether the camera flash/torch is enabled
   */
  isTorchEnabled = async () => {
    this._onHandle('isTorchEnabled');

    // return this._handle && CameraModule.isTorchEnabled(this._handle);
    return true;
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

    // return this._handle && CameraModule.takePicture(this._handle, outputDir);
    return true;
  }

  /**
   * View renderization happens here
   */
  render = () => {
    const { style, ...properties } = this._expandProps(this.props);
    
    // if we were authorized or there's a possibly handler function, let's render the camera
    if (this.hasFaCC()) {
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
  }
}

