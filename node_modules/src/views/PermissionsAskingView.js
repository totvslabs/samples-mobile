import {
  View,
  ActivityIndicator,
  StyleSheet
} from 'react-native';

import React from 'react';

/**
 * View representing the state of the camera while requesting permission.
 * 
 * @param {Object} props 
 */
export default PermissionsAskingView = () => {
  return (
    <View style={[styles.view]}>
      <ActivityIndicator size="small"/>
    </View>
  );
};

const styles = StyleSheet.create({
  view: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  }
});