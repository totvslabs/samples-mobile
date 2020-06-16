/**
 * Export [VisionModule]
 */
export { default } from './modules/VisionModule';
/**
 * Export [FaceCameraView]
 */
export { default, FaceCameraConstants, FaceCameraState } from './views/FaceCameraView';
/**
 * Export [BarcodeCameraView]
 */
export { default, BarcodeCameraConstants, BarcodeCameraState } from './views/BarcodeCameraView';
/**
 * Import & Export camera state
 */
import { type State } from './utils/types';

export type CameraState = State;
