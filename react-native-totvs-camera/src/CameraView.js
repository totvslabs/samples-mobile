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
  requireNativeComponent,
  NativeModules,
  ViewPropTypes,
  View,
  ActivityIndicator,
  Text,  
} from 'react-native';

import React, { Component } from 'react';

/////////////////////////////
// Import Styles
/////////////////////////////

import styles from './styles';

/////////////////////////////
// Import Native Components
/////////////////////////////

const TOTVSCameraView = requireNativeComponent('TOTVSCameraView');

/////////////////////////////
// Import Native Modules
/////////////////////////////

const CameraModule = NativeModules.TOTVSCamera;

/////////////////////////////
// Type System
////////////////////////////

type PropsType = {
  requestPermissions?: boolean,
  permissionDialogTitle?: string, 
  permissionDialogMessage?: string,
  authorizingView?: React.Component,
  unauthorizedView?: React.Component
};

type StateType = {
  isAuthorized: bool
};

export type State = 'READY' | 'AUTHORIZING' | 'UNAUTHORIZED';

/////////////////////////////
// Constants
/////////////////////////////

/**
 * Enum representing camera status
 */
const CameraState = {
  READY: 'READY',
  AUTHORIZING: 'AUTHORIZING',
  UNAUTHORIZED: 'UNAUTHORIZED'
};
/**
 * Constants exposed by the camera manager of the camera view
 */
const Constants = {    
};


/////////////////////////////
// Component & Utilities
/////////////////////////////

/**
 * return true iff value is either null or undefined 
 * 
 * @param {Object} value 
 */
const isAbsent = value => null === value || undefined == value;

/**
 * View that bridge the native implementation of the camera to JS.
 */
class CameraView extends Component<PropsType, StateType> {
  /**
   * Properies exposed by this view
   */
  static propTypes = {
    ...ViewPropTypes,
    requestPermissions: PropTypes.bool,
    permissionDialogTitle: PropTypes.string, 
    permissionDialogMessage: PropTypes.string,
    authorizingView: PropTypes.element,
    unauthorizedView: PropTypes.element
  };
  /**
   * Default props for this view
   */
  static defaultProps = {    
    permissionDialogTitle: '', 
    permissionDialogMessage: '',
    authorizingView: AuthorizingView(),
    unauthorizedView: UnauthorizedView()
  };

  _cameraId;
  _cameraRef;
  _isMounted;

  constructor(props: PropsType) {
    super(props);
    
    // set props default values
    this._isMounted = false;

    // set state initial values
    this.state = { 
      isAuthorized: false
    };
  }

  componentDidMount = async () => {

  }
  
  componentWillUnmount = () => {
    this._isMounted = false;
  }

  /**
   * Set the native view reference
   */
  _setReference = (ref: Object) => {
    this._cameraRef = ref;
    this._cameraId  = ref && findNodeHandle(ref);
  }

  /**
   * Utility function to infer the state of other properties to pass down
   * to the native native. e.g if user code set the `onFramePreview` callback
   * property, we might set automatically the property `framePreviewEnabled` to
   * true in case the user didn't set it explicitly.
   */
  _expandProps = ({ children, ...props } : PropsType) => {
    const expanded: PropsType = { ...props };
    
    // enable permission request in case user enable permission dialor 
    // props and didn't set the allow flag.
    expanded.requestPermissions = (
      props.permissionDialogTitle || 
      props.permissionDialogMessage
    ) && isAbsent(props.requestPermissions) ? true : props.requestPermissions;

    return expanded;
  }

  getState = (): State => {
      
  }

  /**
   * View renderization happens here
   */
  render = () => {
    const { style, ...properties } = this._expandProps(this.props);
    
    return (
      <View style={style}>
        <TOTVSCameraView
          {...properties}
          ref={this._setReference}
          style={styles.cameraView.camera}
        />
        <Children camera={this} {...this.props} />
      </View>
    );
  }
}

/**
 * Render appropriately a child component based on the type. 
 * We use this in case we need to have a child function component that 
 * need access to the camera component.
 */
const Children = ({ camera, ...props }) => {
  const isFunction = e => typeof e === 'function';

  const { children, ...rest } = props;
  
  return isFunction(children) ? children({ camera, ...props }) : children;
}

/**
 * View representing the state of the camera while requesting permission.
 * The caller code can customize this setting `authorizingView` property.
 * 
 * @param {Object} props 
 */
const AuthorizingView = props => {
  return (
    <View style={[styles.authorizingView.view, props.style]}>
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
const UnauthorizedView = props => {
  const text = "Camera not authorized";
  return (
    <View style={[styles.unauthorizedView.view, props.style]}>
      <Text style={[styles.unauthorizedView.text]}>{text}</Text>
    </View>
  );
}

/////////////////////////////
// Exports
////////////////////////////

export { CameraView as default, Constants };





