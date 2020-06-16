/**
 * Export [VisionModule]
 */
export { default as VisionModule } from './modules/VisionModule';
/**
 * Export [FaceCameraView]
 */
export { default as FaceCameraView, FaceCameraConstants, FaceCameraState } from './views/FaceCameraView';
/**
 * Export [BarcodeCameraView]
 */
export { default as BarcodeCameraView, BarcodeCameraConstants, BarcodeCameraState } from './views/BarcodeCameraView';
/**
 * Import & Export camera state
 */
import { type State } from './utils/types';

export type CameraState = State;
