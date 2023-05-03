package com.example.artech.geoapi.hellogeospatial.helpers;

import static com.google.android.gms.common.util.CollectionUtils.setOf;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Set;




/*
This is a Java class named "arcorelifecyclehelper" which implements the "DefaultLifecycleObserver" interface.
It is used to manage the ARCore session's lifecycle in an Android app.

  The class has the following attributes:

        "activity": an Activity object representing the current activity
        "features": a set of ARCore session features
                        1. Environmental HDR: This feature enhances the lighting and color of the AR experience to match the real-world lighting conditions.
                        2. Depth: It enables the AR app to understand the distance between the camera and the real-world objects, which can be used to create realistic occlusion effects.
                        3. Instant Placement: It allows the AR app to place virtual objects instantly on any flat surface, without the need for a surface detection process.
                        4. Image Tracking: This feature enables the AR app to track 2D images and use them as anchors for placing virtual objects.
                        5. Augmented Faces: It enables the AR app to track and map facial features in real-time, allowing users to apply virtual makeup, masks, or other effects on their faces.
        "installRequested": a boolean flag indicating if ARCore installation has been requested
        "session": a Session object representing the ARCore session
        "exceptionCallback": an interface to handle exceptions
                            In Java, an interface is a collection of abstract methods and constants (static final variables).
                            It defines a set of methods that a class implementing the interface must implement.
                            An interface is declared using the interface keyword and the methods defined in the interface are
                            implicitly public and abstract. In addition, an interface can also contain static and default methods.
        "beforeSessionResume": an interface to handle actions before resuming the session

  The class has the following methods:

        "arcorelifecyclehelper": a constructor that initializes the "activity" and "features" attributes
        "setExceptionCallback": a method to set the "exceptionCallback" interface
        "setBeforeSessionResume": a method to set the "beforeSessionResume" interface
        "tryCreateSession": a private method that attempts to create an ARCore session and returns null if it fails
        "onResume": a method called when the activity is resumed, which resumes the ARCore session
        "onPause": a method called when the activity is paused, which pauses the ARCore session
        "onDestroy": a method called when the activity is destroyed, which closes the ARCore session and releases its resources

        Overall, this class is used to manage the lifecycle of the ARCore session and handle any errors
        that may occur during its creation or use.
*/



/**
 * Manages an ARCore Session using the Android Lifecycle API. Before starting a Session, this class
 * requests installation of Google Play Services for AR if it's not installed or not up to date and
 * asks the user for required permissions if necessary.
 */
public class arcorelifecyclehelper implements DefaultLifecycleObserver {
    public static final String TAG = "ARCoreSessionLifecycleHelper";

    public final Activity activity;
    public final Set<Session.Feature> features;

    public boolean installRequested = false;
    public static Session session = null;
    public ExceptionCallback exceptionCallback = null;
    public BeforeSessionResume beforeSessionResume = null;



    public arcorelifecyclehelper(Activity activity) {
        this.activity = activity;
        this.features = setOf();
    }


    public void setExceptionCallback(ExceptionCallback exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
    }

    public void setBeforeSessionResume(BeforeSessionResume beforeSessionResume) {
        this.beforeSessionResume = beforeSessionResume;
    }

    /**
     * Creating a session may fail. In this case, session will remain null, and this function will be
     * called with an exception.
     *
     * See
     * [the `Session` constructor](https://developers.google.com/ar/reference/java/com/google/ar/core/Session#Session(android.content.Context)
     * ) for more details.
     */
    public interface BeforeSessionResume {
        void onBeforeSessionResume(Session session);
    }

    /**
     * Before `Session.resume()` is called, a session must be configured. Use
     * [`Session.configure`](https://developers.google.com/ar/reference/java/com/google/ar/core/Session#configure-config)
     * or
     * [`setCameraConfig`](https://developers.google.com/ar/reference/java/com/google/ar/core/Session#setCameraConfig-cameraConfig)
     */
    public interface ExceptionCallback {
        void onException(Exception e);
    }


    /**
     * Attempts to create a session. If Google Play Services for AR is not installed or not up to
     * date, request installation.
     *
     * @return null when the session cannot be created due to a lack of the CAMERA permission or when
     * Google Play Services for AR is not installed or up to date, or when session creation fails for
     * any reason. In the case of a failure, [exceptionCallback] is invoked with the failure
     * exception.
     */
    private Session tryCreateSession() {
        // The app must have been given the CAMERA permission. If we don't have it yet, request it.
        if (!geopermission.hasGeoPermissions(activity)) {
            geopermission.requestPermissions(activity);
            return null;
        }

        try {
            // Request installation if necessary.
            switch (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    // tryCreateSession will be called again, so we return null for now.
                    return null;
                case INSTALLED:
                    // Left empty; nothing needs to be done.
                    break;
            }

            // Create a session if Google Play Services for AR is installed and up to date.
            return new Session(activity, features);
        } catch (Exception e) {
            if (exceptionCallback != null) {
                exceptionCallback.onException(e);
            } else {
                Log.e(TAG, "Failed to create ARCore session", e);
            }
            return null;
        }
    }


    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        Session session = arcorelifecyclehelper.session;
        if (session == null) {
            session = tryCreateSession();
            if (session == null) {
                return;
            }
            arcorelifecyclehelper.session = session;
        }

        try {
            if (beforeSessionResume != null) {
                beforeSessionResume.onBeforeSessionResume(session);
            }
            session.resume();
        } catch (CameraNotAvailableException e){
            if (exceptionCallback != null) {
                exceptionCallback.onException(e);
            } else {
                Log.e(TAG, "Failed to create ARCore session", e);
            }
        }

    }


    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        if (session != null) {
            session.pause();
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);

        // Explicitly close the ARCore session to release native resources.
        // Review the API reference for important considerations before calling close() in apps with
        // more complicated lifecycle requirements:
        // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()

        if (session != null) {
            session.close();
            session = null;
        }
    }
}
