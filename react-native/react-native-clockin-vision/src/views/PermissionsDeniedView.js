import {
  View,
  Text,
  StyleSheet
} from 'react-native';

import React from 'react';

/**
 * View representing the state of the camera when no permission was granted
 * 
 * @param {Object} props 
 */
export default PermissionsDeniedView = () => {
  const text = "Camera not authorized";
  return (
    <View style={[styles.view]}>
      <Text style={[styles.text]}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  view: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',    
  },
  text: {
    textAlign: 'center',
    fontSize: 16,    
    color: 'black'
  }  
});