package horizon.apps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;

public class AutoCamera extends AutoServiceBindActivity
{
	public static final String	INTENT_AUTOCAMERA_TAKEN = "horizon.apps.AutoCamera.INTENT_AUTOCAMERA_TAKEN";
	public static final int CAMERA_TAKE_TIME = 10;
	public static final int MSG_AUTOCAMERA_TAKEN = 0;

	protected static final String	TAG = "AutoCamera";

	protected Timer imageTakeTimer = null;
	
	public static interface ImageTakenCallback
	{
		public void imageTaken(byte[] image);
	}

	
	
	protected final File imageStore = new File(Environment.getExternalStorageDirectory(), "conversation");

	protected AsyncTask<Void, Void, Camera> cameraSetupTask =
		new AsyncTask<Void, Void, Camera>()
		{
		protected Camera doInBackground(Void... params) 
		{
			Log.d(TAG, "Starting app timer");
			imageTakeTimer = new Timer();
			imageTakeTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					imageTakeTimer_onFire();
				}
			}, CAMERA_TAKE_TIME * 1000,  CAMERA_TAKE_TIME * 1000);

			Log.d(TAG, "Setting up camera");
			Camera camera = null;
			try
			{
				camera = Camera.open();
				Parameters p = camera.getParameters();
				Camera.Size ps = p.getSupportedPictureSizes().get(0);
				p.setPictureSize(ps.width, ps.height);
				Log.i(TAG, "Image size set to " + ps.width + " by " + ps.height);
				camera.getParameters().setRotation(90);

				Camera.Size s = p.getSupportedPreviewSizes().get(0);
				p.setPreviewSize(s.width, s.height);

				p.setPictureFormat(PixelFormat.JPEG);
				p.set("flash-mode", "auto");
				camera.setParameters(p);
			} 
			catch(Exception e)
			{
				Log.e(TAG, e.getMessage());
			}
			return camera;
		}

		private void imageTakeTimer_onFire() {
			Log.i(TAG, "ImageTakeTimer fired");
			imageTakeTimer.cancel();
			onPictureFailed();
		}
		
		@Override
		protected void onPostExecute(Camera result)
		{
			if(result!=null)
			{
				CameraSurfaceView view = new CameraSurfaceView(result, new ImageTakenCallback()
				{
					@Override
					public void imageTaken(byte[] image) 
					{
						Log.d(TAG, "Image taken");
						imageTakeTimer.cancel();
						String fileName = "";
						try
						{                                   
							File f = File.createTempFile("con", ".jpg", imageStore);
							BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f), 8192);
							out.write(image);
							out.close();
							fileName = f.getAbsolutePath();
							Log.i(TAG, "Image saved at:" + fileName);
						}
						catch(Exception e)
						{
							Log.e(TAG, e.getMessage(), e);
						} 
						onPictureReady(fileName);
						
					}
				}, AutoCamera.this);
				LinearLayout ll = (LinearLayout)findViewById(R.id.main);
				ll.addView(view);
			}
			else
			{
				imageTakeTimer.cancel();
				onPictureFailed();
			}
		}

	};

	protected void onPictureReady(String filename)
	{
		Bundle b = new Bundle();
		b.putString("fileName", filename);
		sendMessageToService(MSG_AUTOCAMERA_TAKEN, b);
		finish();
	}


	protected void onPictureFailed()
	{
		Log.e(TAG, "Picture failed");
		finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		Log.d(TAG, "Starting AutoCamera");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if(!imageStore.exists())
			imageStore.mkdir();
		cameraSetupTask.execute((Void[])null);
	}
	


	@Override
	public void onDestroy()
	{
		Log.d(TAG, "Destroying AutoCamera");
		super.onDestroy();
	}

}