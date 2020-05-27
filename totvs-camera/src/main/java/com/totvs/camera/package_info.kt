package com.totvs.camera


/**
 * This module tries to keep up with teh module react-native-totvs-camera. It's mainly
 * used to compile the modules and run then directly into an android environment, we do this
 * to avoid fixing the react-native library compatibility issues. facebook has one version
 * published into their maven repo that is different from the one resolved at runtime
 * from node_modules. This said, if over time you notice an inconsistency on the sources
 * of this project and the one cited above, then just copy the sources from there to here,
 * except the ReactCamera*.kt classes.
  */