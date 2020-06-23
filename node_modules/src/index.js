/**
 * Export [VisionModule]
 */
export { default as VisionModule } from './modules/vision';
/**
 * Export [FaceCameraView]
 */
export { default as FaceCameraView, FaceCameraConstants, FaceCameraState } from './views/Face';
/**
 * Export [BarcodeCameraView]
 */
export { default as BarcodeCameraView, BarcodeCameraConstants, BarcodeCameraState } from './views/Barcode';
/**
 * Import & Export camera state
 */
import { type State } from './utils/types';

export type CameraState = State;
