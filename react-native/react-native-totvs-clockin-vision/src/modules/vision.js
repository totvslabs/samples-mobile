/**
 * @format
 * @flow strict-local
 */

import {
  NativeModules
} from 'react-native';

/////////////////////////////
// Import Native Components
/////////////////////////////

const NativeModule = NativeModules.VisionModule;

/////////////////////////////
// Constants
/////////////////////////////

const BASE_64_DATA_HEADER = 'data:image/jpeg;base64,';

/**
 * This class expose utility functionalities that the clock-in vision module
 * offers and are not attached to any particular view offered by it.
 */
export default class VisionModule {
  /**
   * Set the name of the output and location directory for the recognition model
   * 
   * @param {String} name of the model output directory
   */
  static setModelOutputDirectoryName = name => NativeModule.setModelOutputDirectoryName(name);

  /**
   * Get the location of the model output directory.
   */
  static getModelOutputDirectory = async () => NativeModule.getModelOutputDirectory();

  /**
   * Create the model output and captures output directories
   */
  static setupModelDirectories = async () => NativeModule.setupModelDirectories();

  /**
   * Utility to trigger the recognition model training operation
   */
  static trainRecognitionModel = async () => NativeModule.trainRecognitionModel();

  /**
   * Retrieve the base64 representation of any image located at [path]
   * 
   * @param {String} path of the image to be encoded in base64
   */
  static getImageFileBase64 = async path => {
    const base64 = await NativeModule.getImageFileBase64(path);

    if (!base64 || null == base64) {
      return null;
    }

    return `${BASE_64_DATA_HEADER}${base64}`;
  };

  /**
   * Delete the image located at [path]
   * 
   * @param {String} path of the file to be deleted.
   */
  static deleteImageFile = async path => NativeModule.deleteImageFile(path);
}