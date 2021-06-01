%module FaceRecognizerJNI
%include "std_string.i"

#include <opencv2/opencv.hpp>
#include <android/bitmap.h>


%typemap(jstype) cv::Mat "android.graphics.Bitmap" // the C/C++ type cv::Mat corresponds to the JAVA type org.opencv.core.Mat (jstype: C++ type corresponds to JAVA type)
%typemap(jtype) cv::Mat "Object" // the C/C++ type cv::Mat corresponds to the JAVA intermediary type long. JAVA intermediary types are used in the intermediary JNI class (e.g. swig_exampleJNI.java)
%typemap(jni) cv::Mat "jobject" // the C/C++ type cv::Mat corresponds to the C/C++ JNI type long, which is used in the generated C/C++ JNI functions in e.g. swig_example_wrap.cpp 

%typemap(javain) cv::Mat "$javainput" // javain tells SWIG how to pass the JAVA object to the intermediary JNI class (e.g. swig_exampleJNI.java); see next step also


// the typemap for in specifies how to create the C/C++ object out of the datatype specified in jni
// this is C/C++ code which is injected in the C/C++ JNI function to create the cv::Mat for further processing in the C/C++ code
%typemap(in) cv::Mat {
        AndroidBitmapInfo info;
        void *pixels = 0;
        CV_Assert(AndroidBitmap_getInfo(jenv, $input, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(jenv, $input, &pixels) >= 0);
        CV_Assert(pixels);
        $1.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (false) cvtColor(tmp, $1, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo($1);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, $1, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(jenv, $input);
}

%{
#include "FaceRecognizer.h"
%}

%include "FaceRecognizer.h"
