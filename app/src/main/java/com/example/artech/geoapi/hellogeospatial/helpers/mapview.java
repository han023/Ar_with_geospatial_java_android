package com.example.artech.geoapi.hellogeospatial.helpers;

import static com.example.artech.geoapi.hellogeospatial.helpers.arcorelifecyclehelper.session;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import androidx.annotation.ColorInt;

import com.example.artech.R;
import com.example.artech.geoapi.hellogeospatial.hellogeoactivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Earth;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;


/*
    This code defines a mapview class, which is responsible for managing and updating a Google Maps view.

    The class defines two Marker objects: cameraMarker and earthMarker. cameraMarker is used to represent
    the camera position, while earthMarker is not currently used in the code.

    In the constructor, the GoogleMap object is passed in and various UI settings are configured, such as
     disabling the map toolbar and the indoor level picker, and enabling camera and idle listeners. An
     OnMarkerClickListener is also set, although it does not do anything in this implementation.

    The updateMapPosition() method takes a latitude, longitude, and heading, and updates the position and
    rotation of the cameraMarker on the map. If the camera is currently moving, the method does nothing, but
    if the camera is idle, it moves the camera to center on the updated position of the cameraMarker.

    The createMarker() method takes a color argument and creates a new Marker object with the specified color.
    The createColoredMarkerBitmap() method creates a new bitmap by applying a color filter to the
    ic_navigation_white_48dp drawable resource.
 */


public class mapview {

    public final int CAMERA_MARKER_COLOR = Color.argb(255, 0, 255, 0);
    public final int EARTH_MARKER_COLOR = Color.argb(255, 125, 125, 125);


    boolean setInitialCameraPosition = false;
    public Marker cameraMarker;
    public boolean cameraIdle = true;

    public Marker earthMarker;
    public final hellogeoactivity activity;
    public final GoogleMap googleMap;

    public mapview(hellogeoactivity activity, GoogleMap googleMap) {
        this.activity = activity;
        this.googleMap = googleMap;

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        googleMap.setOnMarkerClickListener(marker -> false);

        googleMap.setOnCameraMoveListener(() -> cameraIdle = false);
        googleMap.setOnCameraIdleListener(() -> cameraIdle = true);

        cameraMarker = createMarker(CAMERA_MARKER_COLOR);
    }



    public void updateMapPosition(double latitude, double longitude, double heading) {
        LatLng position = new LatLng(latitude, longitude);
        activity.runOnUiThread(() -> {
            if (!cameraIdle) {
                return;
            }
            cameraMarker.setVisible(true);
            cameraMarker.setPosition(position);
            cameraMarker.setRotation((float) heading);

            CameraPosition.Builder cameraPositionBuilder;
            if (!setInitialCameraPosition) {
                setInitialCameraPosition = true;
                cameraPositionBuilder = new CameraPosition.Builder().zoom(21f).target(position);
            } else {
                cameraPositionBuilder = new CameraPosition.Builder()
                        .zoom(googleMap.getCameraPosition().zoom)
                        .target(position);
            }
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build()));
        });
    }

    public Marker createMarker(@ColorInt int color) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(0.0, 0.0))
                .draggable(false)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap(createColoredMarkerBitmap(color)));
        return googleMap.addMarker(markerOptions);
    }

    public Bitmap createColoredMarkerBitmap(@ColorInt int color) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        Bitmap navigationIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_navigation_white_48dp, opt);
        Paint p = new Paint();
        p.setColorFilter(new LightingColorFilter(color, 1));
        Canvas canvas = new Canvas(navigationIcon);
        canvas.drawBitmap(navigationIcon, 0f, 0f, p);
        return navigationIcon;
    }

}
