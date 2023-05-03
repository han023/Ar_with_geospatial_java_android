package com.example.artech.geoapi.hellogeospatial;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artech.geoapi.common.helpers.FullScreenHelper;
import com.example.artech.geoapi.common.samplerender.SampleRender;
import com.example.artech.geoapi.hellogeospatial.helpers.arcorelifecyclehelper;
import com.example.artech.geoapi.hellogeospatial.helpers.geopermission;
import com.example.artech.geoapi.hellogeospatial.helpers.hellogeoview;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;





/**

1. Creates an instance of arcorelifecyclehelper to manage the ARCore session lifecycle, and sets up an
    exception callback to handle any errors that occur during session creation or resumption.
2. Configures the ARCore session with geospatial mode enabled, automatic depth mode, and automatic focus mode.
3. Sets up an instance of hellogeorender to render the augmented reality scene, and an instance of
    hellogeoview to manage the user interface.
4. Registers the hellogeorender and hellogeoview instances as observers of the activity's lifecycle,
    so they can be properly managed during the activity's lifecycle events.
5. Sets the content view to the root view of the hellogeoview instance.
6. Implements onRequestPermissionsResult to handle the results of the camera and location permissions
    request, and displays a message and exits the app if the permissions are not granted.
7.Implements onWindowFocusChanged to use the FullScreenHelper class to manage the activity's full-screen display.

 **/




public class hellogeoactivity extends AppCompatActivity  {

    public static String TAG = "HelloGeoActivity";
    public arcorelifecyclehelper arCoreSessionHelper;
    public hellogeoview view;
    public hellogeorender renderer;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ARCore session lifecycle helper and configuration.
        arCoreSessionHelper = new arcorelifecyclehelper(this);
        // If Session creation or Session.resume() fails, display a message and log detailed
        // information.
        arCoreSessionHelper.setExceptionCallback(exception -> {
            // Handle the exception here
            String message;
            if (exception instanceof UnavailableUserDeclinedInstallationException) {
                message = "Please install Google Play Services for AR";
            } else if (exception instanceof UnavailableApkTooOldException) {
                message = "Please update ARCore";
            } else if (exception instanceof UnavailableSdkTooOldException) {
                message = "Please update this app";
            } else if (exception instanceof UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR";
            } else if (exception instanceof CameraNotAvailableException) {
                message = "Camera not available. Try restarting the app.";
            } else {
                message = "Failed to create AR session: " + exception;
            }
            Log.e(TAG, "ARCore threw an exception", exception);
            view.snackbarHelper.showError(hellogeoactivity.this, message);
        });



        // Configure session features.
        arCoreSessionHelper.setBeforeSessionResume(this::configureSession);
        getLifecycle().addObserver(arCoreSessionHelper);

        // Set up the Hello AR renderer.
        hellogeorender.activityg = this;
        renderer = new hellogeorender(this);
        getLifecycle().addObserver(renderer);

        // Set up Hello AR UI.
        view = new hellogeoview(hellogeoactivity.this);
        getLifecycle().addObserver(view);
        setContentView(view.root);

        // Sets up an example renderer using our HelloGeoRenderer.
        new SampleRender(view.surfaceView, renderer, getAssets());



    }

    private void configureSession(Session session){
        session.configure(
                session.getConfig().setGeospatialMode(Config.GeospatialMode.ENABLED)
                        .setDepthMode(Config.DepthMode.AUTOMATIC)
                        .setFocusMode(Config.FocusMode.AUTO)
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!geopermission.hasGeoPermissions(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera and location permissions are needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!geopermission.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                geopermission.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

}
