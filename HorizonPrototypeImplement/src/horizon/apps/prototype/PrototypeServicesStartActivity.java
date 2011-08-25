package horizon.apps.prototype;

import horizon.apps.R;
import horizon.apps.R.layout;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class PrototypeServicesStartActivity extends Activity
{
	private final static String TAG = "PrototypeServicesStartActivity";
	private ArrayList<Class> startedServices = null;

	@Override
	public void onCreate(Bundle b)
	{
		Log.i(TAG, "Creating activity");
		super.onCreate(b);
		startedServices = new ArrayList<Class>(); 
		setContentView(R.layout.main);
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "Destroying started things:");
		stopAllServices();
		super.onDestroy();
	}

	private void startAService(Class serviceName)
	{
		Log.i(TAG, "Starting: " + serviceName);
		Intent serviceToStartIntent = new Intent(getApplicationContext(), serviceName);
		serviceToStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);		
		startService(serviceToStartIntent);
		startedServices.add(serviceName);
	}

	private void stopAService(Class serviceName)
	{
		Log.i(TAG, "Stopping: " + serviceName);
		Intent serviceToStopIntent = new Intent(getApplicationContext(), serviceName);
		stopService(serviceToStopIntent);
		startedServices.remove(serviceName);
	}

	private void stopAllServices()
	{
		for(Class serviceName: startedServices)
		{
			if(serviceName!= null)
			{
				stopAService(serviceName);
			}
		}
	}
	
	public void autoCameraAppButton_onClick(View v)
	{
		Log.d(TAG, "Start AutoCamera clicked");
		startAService(horizon.apps.prototype.AutoCameraImplementService.class);
	}

	public void stopLaunchedServicesButton_onClick(View v)
	{
		Log.d(TAG, "Stopping servies, etc");
		stopAllServices();
	}
	
	public void exitButton_onClick(View v)
	{
		Log.d(TAG, "Exit button clicked");
		finish();
	}

}
