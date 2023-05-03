package com.example.artech.geoapi.hellogeospatial;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.artech.R;
import com.example.artech.geoapi.common.helpers.DisplayRotationHelper;
import com.example.artech.geoapi.common.helpers.TrackingStateHelper;
import com.example.artech.geoapi.common.samplerender.Framebuffer;
import com.example.artech.geoapi.common.samplerender.Mesh;
import com.example.artech.geoapi.common.samplerender.SampleRender;
import com.example.artech.geoapi.common.samplerender.Shader;
import com.example.artech.geoapi.common.samplerender.Texture;
import com.example.artech.geoapi.common.samplerender.arcore.BackgroundRenderer;
import com.example.artech.geoapi.hellogeospatial.helpers.arcorelifecyclehelper;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Earth;
import com.google.ar.core.Frame;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;


/**

    The hellogeorender class is the main rendering class, and it implements the SampleRender.
    Renderer interface. This class contains a number of member variables, including:

        1. TAG and Z_NEAR, Z_FAR are constants used to set up the camera's frustum. The frustum is the portion
            of 3D space that is visible in the camera's view.
        2. backgroundRenderer is an object used to render the camera's background image.
        3. virtualSceneFramebuffer is a framebuffer object used to store the virtual scene rendered by the app.
        4. virtualObjectMesh, virtualObjectShader, and virtualObjectTexture are objects used to render the geospatial marker.

    The class also contains a number of matrices that are used to transform the virtual object into the camera's view.

    The onResume and onPause methods are lifecycle methods that are called when the app enters and leaves the
    foreground, respectively. They are used to pause and resume the rendering process.

    The onSurfaceCreated, onSurfaceChanged, and onDrawFrame methods are also part of the rendering process.
    They are called by the rendering engine to create, update, and draw the app's graphics. In the onDrawFrame
    method, the app updates the camera's view and renders the virtual scene to the screen.

**/


public class hellogeorender implements SampleRender.Renderer, DefaultLifecycleObserver {

    public static hellogeoactivity activityg ;
    public hellogeorender(hellogeoactivity activityy) {
        activityg = activityy;
    }

    public  static String TAG = "HelloGeoRenderer";
    public  static float Z_NEAR = 0.1f;
    public  static float Z_FAR = 1000f;

    public BackgroundRenderer backgroundRenderer;
    public Framebuffer virtualSceneFramebuffer;
    public boolean hasSetTextureNames = false;

    // Virtual object (ARCore pawn)
    public Mesh virtualObjectMesh;
    public Shader virtualObjectShader;
    public Texture virtualObjectTexture;

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    float[] modelMatrix = new float[16];
    float[] viewMatrix = new float[16];
    float[] projectionMatrix = new float[16];
    float[] modelViewMatrix = new float[16]; // view x model
    float[] modelViewProjectionMatrix = new float[16];


    public Session session;
    public Session getSession() {
        return arcorelifecyclehelper.session;
    }


    DisplayRotationHelper displayRotationHelper  = new DisplayRotationHelper(activityg);
    TrackingStateHelper trackingStateHelper = new TrackingStateHelper(activityg);


    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        displayRotationHelper.onResume();
        hasSetTextureNames = false;
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        displayRotationHelper.onPause();
    }



    @Override
    public void onSurfaceCreated(SampleRender render) {
        // Prepare the rendering objects.
        // This involves reading shaders and 3D model files, so may throw an IOException.
        try {
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);

            // Virtual object to render (Geospatial Marker)
            virtualObjectTexture =
                    Texture.createFromAsset(render, "models/spatial_marker_baked.png", Texture.WrapMode.CLAMP_TO_EDGE, Texture.ColorFormat.SRGB);

            virtualObjectMesh = Mesh.createFromAsset(render, "models/geospatial_marker.obj");
            virtualObjectShader =
                    Shader.createFromAssets(render, "shaders/ar_unlit_object.vert", "shaders/ar_unlit_object.frag", /*defines=*/ null)
                            .setTexture("u_Texture", virtualObjectTexture);

            backgroundRenderer.setUseDepthVisualization(render, false);
            backgroundRenderer.setUseOcclusion(render, false);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            showError("Failed to read a required asset file: " + e);
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        Session session = getSession();
        if (session == null) {
            return;
        }

        // -- ARCore frame boilerplate

        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        // -- Update per-frame state

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        Frame frame = null;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            showError("Camera not available. Try restarting the app.");
            return;
        }

        Camera camera = frame.getCamera();

        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame);

        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        // -- Draw background
        if (frame.getTimestamp() != 0L) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }

        // If not tracking, don't draw 3D objects.
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0,  Z_NEAR,  Z_FAR);

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0);

        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);

        // TODO: Obtain Geospatial information and display it on the map.
        Earth earth = session.getEarth();
        if (earth != null && earth.getTrackingState() == TrackingState.TRACKING) {
            final GeospatialPose cameraGeospatialPose = earth.getCameraGeospatialPose();


            activityg.view.mapView.updateMapPosition(
                    cameraGeospatialPose.getLatitude(),
                    cameraGeospatialPose.getLongitude(),
                    cameraGeospatialPose.getHeading()
            );
        }
        assert earth != null;
        activityg.view.updateStatusText(earth, earth.getCameraGeospatialPose());

        // Draw the placed anchor, if it exists.
        if (earthAnchor != null) {
            render.renderCompassAtAnchor(earthAnchor,viewMatrix,modelMatrix,projectionMatrix,modelViewMatrix,
                    modelViewProjectionMatrix,virtualObjectShader,virtualObjectMesh,virtualSceneFramebuffer);
        }





        // Compose the virtual scene with the background.
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR,  Z_FAR);
    }


    public Anchor earthAnchor = null;

    public void onMapClick(LatLng latLng) {
        // TODO: place an anchor at the given position.
        Earth earth = session.getEarth();
        if (earth == null || earth.getTrackingState() != TrackingState.TRACKING) {
            return;
        }
        if (earthAnchor != null) {
            earthAnchor.detach();
        }
        earthAnchor = earth.createAnchor(
                latLng.latitude, latLng.longitude, earth.getCameraGeospatialPose().getAltitude() - 1.3f,
                0f, 0f, 0f, 1f
        );
        if (activityg.view.mapView != null) {
            activityg.view.mapView.earthMarker.setPosition(latLng);
            activityg.view.mapView.earthMarker.setVisible(true);
        }
    }


    private void showError(String errorMessage) {
        activityg.view.snackbarHelper.showError(activityg, errorMessage);
    }



}
