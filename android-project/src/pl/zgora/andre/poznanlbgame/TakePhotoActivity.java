package pl.zgora.andre.poznanlbgame;

import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

/** (not used - MediaStore works better) based on "Android Wireless Application Development" By Shane Conder and Lauren Darcey
 * code sample (chapter 15) available in "Downloads" section:
 * http://www.informit.com/store/product.aspx?isbn=9780321743015 */

public class TakePhotoActivity extends Activity
{
	private static final String TAG 		  = TakePhotoActivity.class.getSimpleName();
	public static final String	K_PHOTO_DATA  = "photo_data";
	public static final int		RESULT_FAILED = -1024;
	
	private static final int	JPEG_QUALITY = 90;
	private static final int 	IMG_WIDTH	 = 800;
	private static final int 	IMG_HEIGHT	 = 480;
	
	private int cameraId = -1;
	private CameraSurfaceView cameraView;
	int currentZoomLevel = 0, 
			maxZoomLevel = 0;
	ZoomControls zoomControls;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_photo);
        
        cameraId = getBackfaceCameraId();
        
        cameraView = new CameraSurfaceView(getApplicationContext());
        FrameLayout frame = (FrameLayout) findViewById(R.id.frPhoto);
        frame.addView(cameraView);
        
        zoomControls = (ZoomControls) findViewById(R.id.zcZoom);
	}
	
	@Override
    public void onResume()
    {
    	super.onResume();
    }
	
	@Override
    public void onPause()
    {
    	super.onPause();
    }
	
	@Override
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.i(TAG, "onDestroy()");
    }
	
	public void btTakePhotoOnClick(android.view.View view)
	{
		Button btTakePhoto = (Button) findViewById(R.id.btTakePhoto);
		btTakePhoto.setEnabled(false);
		
		Log.v(TAG, "Requesting capture");
        cameraView.capture(
        	new Camera.PictureCallback()
        	{
        		public void onPictureTaken(byte[] data, Camera camera)
        		{
        			Log.v(TAG, "Image data received from camera, " + data.length + " bytes");
        			
        			Intent intent = getIntent();
        			intent.putExtra(K_PHOTO_DATA, data);
        			setResult(RESULT_OK, intent);
        			finish();
        		}
        	}
        );
	}
	
	private int getBackfaceCameraId()
	{
		SharedPreferences sp = getApplicationContext().getSharedPreferences(StartActivity.GAME_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
    	int id = sp.getInt(StartActivity.GAME_KEY_CAMERA_ID, -1);
		if (id < 0)
			Log.e(TAG, "No back-facing camera");
		
		return id;
	}
	
	private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback
	{
	    private Camera camera = null;
	    private SurfaceHolder mHolder = null;
	
	    public CameraSurfaceView(Context context)
	    {
	        super(context);
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }
	
	    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	    {
	        try
	        {
	            Camera.Parameters params = camera.getParameters();
	            params.setPictureFormat(ImageFormat.JPEG);
	            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
	            params.setJpegQuality(JPEG_QUALITY);
	            params.setPictureSize(IMG_WIDTH, IMG_HEIGHT);
	            // not all cameras supporting setting arbitrary sizes
	            List<Size> sizes = params.getSupportedPreviewSizes();
	            Camera.Size pickedSize = getBestFit(sizes, width, height);
	            
	            if (params.isZoomSupported())
	            {
	            	maxZoomLevel = params.getMaxZoom();

	            	zoomControls.setIsZoomInEnabled(true);
            	    zoomControls.setIsZoomOutEnabled(true);

            	    zoomControls.setOnZoomInClickListener(new OnClickListener(){
            	        public void onClick(View v){
            	                if(currentZoomLevel < maxZoomLevel){
            	                    currentZoomLevel++;
            	                    Log.d(TAG, "Zoom level: " + currentZoomLevel);
            	                    camera.getParameters().setZoom(currentZoomLevel);
            	                }
            	        }
            	    });

            	zoomControls.setOnZoomOutClickListener(new OnClickListener(){
            	        public void onClick(View v){
            	                if(currentZoomLevel > 0){
            	                    currentZoomLevel--;
            	                    Log.d(TAG, "Zoom level: " + currentZoomLevel);
            	                    camera.getParameters().setZoom(currentZoomLevel);
            	                }
            	        }
            	    });    
	            }
	            else
	            	zoomControls.hide();
	            
	            if (pickedSize != null)
	            {
	                params.setPreviewSize(pickedSize.width, pickedSize.height);
	                Log.d(TAG, "Preview size: (" + pickedSize.width + "," + pickedSize.height + ")");
	                // even after setting a supported size, the preview size may
	                // still end up just being the surface size (supported or not)
	                camera.setParameters(params);
	            }

	            // android-dev snippet
	            Camera.CameraInfo info = new Camera.CameraInfo();
	            Camera.getCameraInfo(cameraId, info);
	            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	            int rotation = display.getRotation();
	            int degrees = 0;
	            switch (rotation) {
	                case Surface.ROTATION_0: degrees = 0; break;
	                case Surface.ROTATION_90: degrees = 90; break;
	                case Surface.ROTATION_180: degrees = 180; break;
	                case Surface.ROTATION_270: degrees = 270; break;
	            }

	            int result = (info.orientation - degrees + 360) % 360;
	            
	            camera.setDisplayOrientation(result);
	            camera.startPreview();
	        }
	        catch (Exception e)
	        {
	            Log.e(TAG, "Failed to set preview size", e);
	        }
	    }
	
	    private Size getBestFit(List<Size> sizes, int width, int height)
	    {
	        Size bestFit = null;
	        ListIterator<Size> items = sizes.listIterator();
	        while (items.hasNext())
	        {
	            Size item = items.next();
	            if (item.width <= width && item.height <= height)
	            {
	                if (bestFit != null)
	                {
	                    // if our current best fit has a smaller area, then we
	                    // want the new one (bigger area == better fit)
	                    if (bestFit.width * bestFit.height < item.width * item.height)
	                    {
	                        bestFit = item;
	                    }
	                } 
	                else
	                {
	                    bestFit = item;
	                }
	            }
	        }
	        return bestFit;
	    }
	
	    public void surfaceCreated(SurfaceHolder holder)
	    {
	    	try
	    	{
	    		camera = Camera.open(cameraId);
	    	}
	    	catch (RuntimeException e)
	    	{
	    		Log.e(TAG, e.toString());
	    		setResult(RESULT_FAILED, getIntent());
    			finish();
	    	}
	        
	        try
	        {
	            camera.setPreviewDisplay(mHolder);
	        }
	        catch (Exception e)
	        {
	            Log.e(TAG, "Failed to set camera preview display", e);
	        }
	    }
	
	    public void surfaceDestroyed(SurfaceHolder holder)
	    {
	        camera.stopPreview();
	        camera.release();
	        camera = null;
	    }
	
	    public boolean capture(Camera.PictureCallback jpegHandler)
	    {
	        if (camera != null)
	        {
	            camera.takePicture(null, null, jpegHandler);
	            return true;
	        } 
	        else
	        {
	            return false;
	        }
	    }
	}
}
