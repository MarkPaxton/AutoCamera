package horizon.apps.prototype;

import horizon.apps.AutoCamera;
import horizon.util.Base64;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class AutoCameraImplementService extends Service
{
	protected final String TAG = "AutoCameraImplementService";

	final Messenger serviceMessenger = new Messenger(new IncomingHandler());



	protected String lastImage = "";

	@Override
	public void onCreate() 
	{
		Log.i(TAG, TAG + " Created");
		super.onCreate();

		Log.d(TAG, "Getting photo");
		Intent i = new Intent();
		i.putExtra("target_intent", "horizon.apps.prototype.AutoCameraImplementService");
		i.setComponent(new ComponentName("horizon.apps", "horizon.apps.AutoCamera"));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, TAG + " Destroyed");
		super.onDestroy();
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what)
			{
			case AutoCamera.MSG_AUTOCAMERA_TAKEN:
				Log.i(TAG, "**********PICTURE RECEIVED!!!!");            
				Bundle b = (Bundle) msg.obj;
				String fileName = b.getString("fileName");
				Log.d(TAG, "Picture received from AutoCamera: " + fileName);
				try
				{
					lastImage = Base64.encodeFromFile(fileName);
					Log.d(TAG, "Read image data from " + fileName);
					new File(fileName).delete();
				}
				catch(IOException e)
				{
					Log.e(TAG, "Error reading image data", e);
				}
				break;	

			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * When binding to the service, we return an interface to our messenger
	 * for sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
	    Log.d(TAG, "Bound" + intent);
		return serviceMessenger.getBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{
	    Log.d(TAG, "Unbound" + intent);
	    return true;
	}
	

}
