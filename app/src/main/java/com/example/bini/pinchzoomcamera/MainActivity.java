package com.example.bini.pinchzoomcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.SessionType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CameraView cameraView;
    private Button VideoIcon, CameraScreen, RecordVideo, CameraIcon;
    private TextView ZoomScale;
    private LinearLayout LayoutText;
    private RelativeLayout LayoutScale;
    private String TAG = "CameraFragment";
    private Bitmap ImageScale, MainImage, ImageText,NewImage = null;
    private File root, dir;
    private boolean Recording = false;
    private double ScreeSize, ActualScreenSize = 710, ScaleSize, ActualScaleSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZoomScale = (TextView)findViewById(R.id.tv_zoom);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth = outMetrics.widthPixels / density;
        ScreeSize = dpWidth;
        ScaleSize = ScreeSize / 5.00;
        ActualScaleSize = (ScaleSize * ActualScreenSize) / ScreeSize;
        cameraView = (CameraView) findViewById(R.id.cameraview);
        CameraIcon = (Button) findViewById(R.id.captureimage);
        VideoIcon = (Button) findViewById(R.id.video_capture);
        RecordVideo = (Button) findViewById(R.id.record_Start);
        CameraScreen = (Button) findViewById(R.id.camera);

        LayoutScale = (RelativeLayout) findViewById(R.id.rl_camera);
        LayoutText = (LinearLayout) findViewById(R.id.rl_textview);

        CameraScreen.setOnClickListener(this);
        VideoIcon.setOnClickListener(this);
        CameraIcon.setOnClickListener(this);
        RecordVideo.setOnClickListener(this);

        dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Foldscope");

        ZoomScale.setText((new DecimalFormat("##.##").format(ActualScaleSize)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
        cameraView.setSessionType(SessionType.PICTURE);
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        cameraView.mapGesture(Gesture.LONG_TAP, GestureAction.CAPTURE); // Long tap to shoot!
        cameraView.addCameraListener(new CameraListener() {

            /**
             * Notifies that the camera was opened.
             * The options object collects all supported options by the current camera.
             */
            @Override
            public void onCameraOpened(CameraOptions options) {
            }

            /**
             * Notifies that the camera session was closed.
             */
            @Override
            public void onCameraClosed() {
            }

            /**
             * Notifies about an error during the camera setup or configuration.
             * At the moment, errors that are passed here are unrecoverable. When this is called,
             * the camera has been released and is presumably showing a black preview.
             *
             * This is the right moment to show an error dialog to the user.
             */
            @Override
            public void onCameraError(CameraException error) {
            }

            /**
             * Notifies that a picture previously captured with capturePicture()
             * or captureSnapshot() is ready to be shown or saved.
             *
             * If planning to get a bitmap, you can use CameraUtils.decodeBitmap()
             * to decode the byte array taking care about orientation.
             */
            @Override
            public void onPictureTaken(byte[] picture) {

                MainImage = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                String deviceMan = android.os.Build.MANUFACTURER;
                if (deviceMan.matches("samsung") || deviceMan.matches("Genymotion")) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    NewImage = Bitmap.createBitmap(MainImage, 0, 0, MainImage.getWidth(), MainImage.getHeight(), matrix, true);
                } else {
                    NewImage = MainImage;
                }
                LayoutScale.setDrawingCacheEnabled(true);
                LayoutScale.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                LayoutScale.layout(0, 0, LayoutScale.getMeasuredWidth(), LayoutScale.getMeasuredHeight());
                LayoutScale.buildDrawingCache(true);
                Bitmap b = Bitmap.createBitmap(LayoutScale.getDrawingCache());
                LayoutScale.setDrawingCacheEnabled(false);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                LayoutText.setDrawingCacheEnabled(true);
                LayoutText.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                LayoutText.layout(0, 0, LayoutText.getMeasuredWidth(), LayoutText.getMeasuredHeight());
                LayoutText.buildDrawingCache(true);
                Bitmap b1 = Bitmap.createBitmap(LayoutText.getDrawingCache());
                ImageScale = Bitmap.createScaledBitmap(b, (int) getResources().getDimension(R.dimen.dp200), (int) getResources().getDimension(R.dimen.dp120), false);
                ImageText = Bitmap.createScaledBitmap(b1, (int) getResources().getDimension(R.dimen.dp100), (int) getResources().getDimension(R.dimen.dp60), false);
                Bitmap OriginalImage = OverlayImage(NewImage, ImageScale, ImageText);
                SaveImage(OriginalImage);
            }

            /**
             * Notifies that a video capture has just ended. The file parameter is the one that
             * was passed to startCapturingVideo(File), or a fallback video file.
             */
            @Override
            public void onVideoTaken(File video) {
            }

            /**
             * Notifies that the device was tilted or the window offset changed.
             * The orientation passed can be used to align views (e.g. buttons) to the current
             * camera viewport so they will appear correctly oriented to the user.
             */
            @Override
            public void onOrientationChanged(int orientation) {
            }

            /**
             * Notifies that user interacted with the screen and started focus with a gesture,
             * and the autofocus is trying to focus around that area.
             * This can be used to draw things on screen.
             */
            @Override
            public void onFocusStart(PointF point) {
            }

            /**
             * Notifies that a gesture focus event just ended, and the camera converged
             * to a new focus (and possibly exposure and white balance).
             */
            @Override
            public void onFocusEnd(boolean successful, PointF point) {
            }

            /**
             * Noitifies that a finger gesture just caused the camera zoom
             * to be changed. This can be used, for example, to draw a seek bar.
             */
            @Override
            public void onZoomChanged(float newValue, float[] bounds, PointF[] fingers) {
                System.out.println("Zoom"+cameraView.getZoom());

                double ActualScaleSize1 = ActualScaleSize*(cameraView.getZoom()/10);

                ZoomScale.setText((new DecimalFormat("##.##").format(ActualScaleSize1)));

            }

            /**
             * Noitifies that a finger gesture just caused the camera exposure correction
             * to be changed. This can be used, for example, to draw a seek bar.
             */
            @Override
            public void onExposureCorrectionChanged(float newValue, float[] bounds, PointF[] fingers) {
            }

        });

    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.captureimage:
                cameraView.capturePicture();
                break;

            case R.id.video_capture:

                VideoIcon.setVisibility(View.INVISIBLE);
                CameraIcon.setVisibility(View.INVISIBLE);
                RecordVideo.setVisibility(View.VISIBLE);
                CameraScreen.setVisibility(View.VISIBLE);
                cameraView.setSessionType(SessionType.VIDEO);

                break;

            case R.id.camera:
                if (Recording)
                {
                    Toast.makeText(this,"Please Stop Video Capture.",Toast.LENGTH_LONG).show();
                }
                else
                {
                    cameraView.setSessionType(SessionType.PICTURE);
                    VideoIcon.setVisibility(View.VISIBLE);
                    CameraScreen.setVisibility(View.INVISIBLE);
                    CameraIcon.setVisibility(View.VISIBLE);
                    RecordVideo.setVisibility(View.INVISIBLE);
                }

                break;

            case R.id.record_Start:
                if (!Recording) {
                    RecordVideo.setBackgroundResource(R.drawable.recordred);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File file = new File(dir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
                    cameraView.startCapturingVideo(file);
                    Recording = true;
                } else {
                    RecordVideo.setBackgroundResource(R.drawable.recordblack);
                    cameraView.stopCapturingVideo();
                    Recording = false;
                }
                break;
        }

    }

    private void SaveImage(Bitmap finalBitmap) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Foldscope");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "IMG_" + timeStamp + ".jpg";
        File file = new File(mediaStorageDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Intent intent = getIntent();
            finish();
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap OverlayImage(Bitmap bitmap1, Bitmap bitmap2, Bitmap bitmap3) {

        Bitmap overlayBitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(overlayBitmap);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, (int) (canvas.getWidth() - bitmap2.getWidth() * 1.6), (int) (canvas.getHeight() - bitmap2.getHeight() * 1.6), null);
        canvas.drawBitmap(bitmap3, canvas.getWidth() - (int) (bitmap3.getWidth() * 2.7), (int) (canvas.getHeight() - bitmap3.getHeight() * 3.5), null);
        return overlayBitmap;
    }
}
