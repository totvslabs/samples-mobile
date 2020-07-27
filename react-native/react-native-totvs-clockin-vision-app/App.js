/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
} from 'react-native';

import { VisionModule } from 'react-native-totvs-clockin-vision';


const App = () => {
  const performAction = async () => {
    const path = await VisionModule.getModelOutputDirectory();
    console.log('getting library result: ', { path });
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity onPress={performAction}>
        <Text>{'Perform'}</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1, 
    alignItems: 'center', 
    alignContent: 'center', 
    justifyContent: 'center'
  }
});

export default App;
