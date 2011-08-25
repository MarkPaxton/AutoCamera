package horizon.apps;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView 
extends SurfaceView
implements SurfaceHolder.Callback
{	
	private final Camera camera;
	
	private final AutoCamera.ImageTakenCallback imageTakenCallback;
	
	public CameraSurfaceView(
	        Camera camera,
	        AutoCamera.ImageTakenCallback imageTakenCallback,
	        Context context) 
	{
		super(context);
		this.camera = camera;
		this.imageTakenCallback = imageTakenCallback;
		SurfaceHolder holder = getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(
			SurfaceHolder holder, int format, int width, int height)
	{
	    camera.stopPreview();
	    camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		try 
		{
			camera.setPreviewDisplay(getHolder());
		}
		catch(IOException e) 
		{
			throw new RuntimeException(e);
		}
		camera.startPreview();
		camera.autoFocus(new Camera.AutoFocusCallback() 
		{	
			@Override
			public void onAutoFocus(boolean success, Camera camera) { 
				camera.takePicture(null, null, new Camera.PictureCallback() 
		        {
		            @Override
		            public void onPictureTaken(byte[] data, Camera camera) 
		            {
		                imageTakenCallback.imageTaken(data);
		            }
		        });

			}
		});
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		camera.stopPreview();
		camera.release();
	}
}
