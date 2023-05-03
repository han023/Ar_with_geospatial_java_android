package com.example.artech.geoapi.hellogeospatial.helpers;

import android.app.Activity;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.artech.R;
import com.example.artech.geoapi.common.helpers.SnackbarHelper;
import com.example.artech.geoapi.hellogeospatial.hellogeoactivity;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Earth;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;

/**
    This Java code defines a class called hellogeoview that implements the DefaultLifecycleObserver interface.
    It contains fields and methods that enable the display and interaction with a Google Maps view in an ARCore app.

    The hellogeoview class has several member variables, including an Activity object, a View object for the layout,
    a GLSurfaceView object for rendering graphics, a SnackbarHelper object for displaying snackbar messages, a mapview
    object for displaying the Google Maps view, and a maptouchwrapper object for handling touch events on the map.

    The constructor for hellogeoview takes an hellogeoactivity object and initializes the member variables. It inflates
    the layout defined in R.layout.hellogeoactivity and assigns the GLSurfaceView and maptouchwrapper objects to their
    respective views. It also retrieves a SupportMapFragment object from the activity's support fragment manager and
    initializes the mapview object when the Google Map is ready.

    The getSession() method returns the ARCore session associated with the arcorelifecyclehelper object.

    The updateStatusText() method updates the status text view with information about the earth state, tracking state,
    and camera geospatial pose. It runs on the UI thread using activity.runOnUiThread().

    Finally, the onResume() and onPause() methods are lifecycle methods that handle pausing and resuming the GLSurfaceView
    when the activity is paused or resumed.
**/



/** Contains UI elements for Hello Geo. */
public class hellogeoview  implements DefaultLifecycleObserver {

    public final Activity activity;
    public View root;
    public GLSurfaceView surfaceView;
    public SnackbarHelper snackbarHelper = new SnackbarHelper();
    public mapview mapView;
    public maptouchwrapper mapTouchWrapper;

    public hellogeoview(hellogeoactivity activity) {
        this.activity = activity;
        root = View.inflate(activity, R.layout.hellogeoactivity, null);
        surfaceView = root.findViewById(R.id.surfaceview);
        mapTouchWrapper = root.findViewById(R.id.map_wrapper);
        mapTouchWrapper.setup(screenLocation -> {
            LatLng latLng = mapView.googleMap.getProjection().fromScreenLocation(screenLocation);
            activity.renderer.onMapClick(latLng);
        });
        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> mapView = new mapview(activity, googleMap));
        }
        TextView statusText = root.findViewById(R.id.statusText);
        statusText.setText(activity.getResources().getString(R.string.earth_state, "", "", ""));
    }



    public Session getSession() {
        return arcorelifecyclehelper.session;
    }

    public void updateStatusText(Earth earth, GeospatialPose cameraGeospatialPose) {
        activity.runOnUiThread(() -> {
            String poseText = "";
            if (cameraGeospatialPose != null) {


                ImageView img = root.findViewById(R.id.statusimg);

                if( (cameraGeospatialPose.getLatitude() >= 29.34395 && cameraGeospatialPose.getLatitude() <= 29.34420)
                && (cameraGeospatialPose.getLongitude() >= 48.08350 && cameraGeospatialPose.getLongitude() <= 48.08400)
                ){
                    img.setVisibility(View.VISIBLE);
                    img.setImageDrawable(activity.getDrawable(R.drawable.a));
                } else if((cameraGeospatialPose.getLatitude() >= 29.34352 && cameraGeospatialPose.getLatitude() <= 29.34395)
                        && (cameraGeospatialPose.getLongitude() >= 48.08307 && cameraGeospatialPose.getLongitude() <= 48.08350)){
                    img.setVisibility(View.VISIBLE);
                    img.setImageDrawable(activity.getDrawable(R.drawable.b));
                } else if((cameraGeospatialPose.getLatitude() >= 29.34420 && cameraGeospatialPose.getLatitude() <= 29.34460)
                        && (cameraGeospatialPose.getLongitude() >= 48.08350 && cameraGeospatialPose.getLongitude() <= 48.08385)){
                    img.setVisibility(View.VISIBLE);
                    img.setImageDrawable(activity.getDrawable(R.drawable.c));
                }else {
                    img.setVisibility(View.INVISIBLE);
                }

                poseText = activity.getString(R.string.geospatial_pose,
                        cameraGeospatialPose.getLatitude(),
                        cameraGeospatialPose.getLongitude(),
                        cameraGeospatialPose.getHorizontalAccuracy(),
                        cameraGeospatialPose.getAltitude(),
                        cameraGeospatialPose.getVerticalAccuracy(),
                        cameraGeospatialPose.getHeading(),
                        cameraGeospatialPose.getHeadingAccuracy()
                        );
            }
            TextView statusText = root.findViewById(R.id.statusText);
            statusText.setText(
                    activity.getResources().getString(R.string.earth_state,
                    earth.getEarthState().toString(),
                    earth.getTrackingState().toString(),
                    poseText)
            );
        });
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        surfaceView.onResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        surfaceView.onPause();
    }



}
