package com.tzutalin.dlib;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.Arrays;
import java.util.List;

/**
 * Created by houzhi on 16-10-20.
 * Modified by tzutalin on 16-11-15
 * Modified by Gaurav on Feb 23, 2018
 */
public class FaceRec {
    private static final String TAG = "dlib";

    // accessed by native methods
    @SuppressWarnings("unused")
    private long mNativeFaceRecContext;
    private String dir_path = "";

    static {
        try {
            System.loadLibrary("android_dlib");
            jniNativeClassInit();
            Log.d(TAG, "jniNativeClassInit success");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "library not found");
        }
    }

    //////////////////////////
    // Constructor
    /////////////////////////

    public FaceRec(String sample_dir_path) {
        dir_path = sample_dir_path;
        jniInit(dir_path);
    }


    //////////////////////////
    // Public Methods
    /////////////////////////

    @Nullable
    @WorkerThread
    public void train() {
        jniTrain();
        return;
    }
    
    @Nullable
    @WorkerThread
    public List<VisionDetRet> recognize(@NonNull Bitmap bitmap) {

        if(bitmap != null){

            VisionDetRet[] detRets = jniBitmapRec(bitmap);
            List<VisionDetRet> result = Arrays.asList(detRets);

            return result;
        }

        return null;

    } 

    public List<VisionDetRet> detect(@NonNull Bitmap bitmap) {
        VisionDetRet[] detRets = jniBitmapDetect(bitmap);
        return Arrays.asList(detRets);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    public void release() {
        jniDeInit();
    }

    @Keep
    private native static void jniNativeClassInit();

    @Keep
    private synchronized native int jniInit(String sample_dir_path);

    @Keep
    private synchronized native int jniDeInit();

    @Keep
    private synchronized native int jniTrain();

    @Keep
    private synchronized native int jniTrainHam();

    @Keep
    private synchronized native VisionDetRet[] jniBitmapDetect(Bitmap bitmap);

    @Keep
    private synchronized native VisionDetRet[] jniBitmapRec(Bitmap bitmap);

    @Keep
    private synchronized native VisionDetRet[] jniBitmapRec();

    // @Keep
    // private native static void jniNativeClassInit();

    // @Keep
    // private synchronized native int jniInit(String sample_dir_path);

    // @Keep
    // private synchronized native int jniDeInit();

    // @Keep
    // private synchronized native int jniTrain();

    // @Keep
    // private synchronized native VisionDetRet[] jniBitmapDetect(Bitmap bitmap);

    // @Keep
    // private synchronized native VisionDetRet[] jniBitmapRec(Bitmap bitmap);
}
