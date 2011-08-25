/**
 * 
 */
package horizon.apps;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author pszmp
 * Generic activity that is started from an intent and then binds to that service to create a messenger thing 
 */
public class AutoServiceBindActivity extends Activity {

	static final String TAG = "AutoServiceBindActivity";

	protected String	serviceName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String targetToBindTo = extras.getString("target_intent");
			serviceName = targetToBindTo;
			if(serviceName!=null)
			{
				doBindService();
			}
		}
	}

	protected void onDestroy()
	{
		if(serviceName!=null)
		{
			doUnbindService();
		}
		super.onDestroy();
	}

	// Deals with background service
	protected Messenger		messengerService	= null;
	protected boolean		serviceIsBound	= false;
	final Messenger			incomingMessenger	= new Messenger(new IncomingHandler());



	/**
	 * Bind the activity to the background service
	 */
	void doBindService()
	{
		Log.d(TAG, "Binding service to " + serviceName);
		Intent i = new Intent();
		i.putExtra("target_intent", serviceName);
		i.setComponent(new ComponentName(serviceName.substring(0, serviceName.lastIndexOf('.')), serviceName));
		Boolean success = bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
		serviceIsBound = success;
		Log.d(TAG, "Binding result: " + (success? "yes" :"no"));
	}

	/**
	 * Unbind this activity from the background service - this should allow the
	 * service to stop properly if it's not bound to anything else and it's not
	 * started explicitly (which it should be for this app).
	 */
	void doUnbindService()
	{
		Log.d(TAG, "Unbinding service...");
		if (serviceIsBound)
		{
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (messengerService != null)
			{
				Log.d(TAG, "Unbinding");
				unbindService(serviceConnection);
				serviceIsBound = false;
			}
		}
	}

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private final ServiceConnection	serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(
				final ComponentName className,
				final IBinder service)
		{
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating
			// with the service using a Messenger, so here we get a
			// client-side representation of that from the raw
			// IBinder object.
			messengerService = new Messenger(service);
			serviceIsBound = true;
			OnServiceConnected(className, service);
		}

		@Override
		public void onServiceDisconnected(
				final ComponentName className)
		{
			// This is called when the connection with
			// the service has been unexpectedly
			// disconnected -- that is, its process crashed.
			serviceIsBound = false;
			messengerService = null;
			OnServiceDisconnected(className);
		}
	};

	public void OnServiceConnected(final ComponentName className, final IBinder service)
	{
		Log.d(TAG, "Service is bound to " + className);
	}

	public void OnServiceDisconnected(final ComponentName className)
	{
		Log.d(TAG, "Service is disconnected (unbound)");
	}

	protected void sendMessageToService(final int messageType)
	{
		Log.d(TAG, "Sending message to service: " + messageType);
		final Message msg = Message.obtain();
		msg.what = messageType;
		msg.replyTo = incomingMessenger;
		new SendMessageTask().execute(msg);
	}

	protected void sendMessageToService(final int messageType, final Parcelable data)
	{
		final Message msg = Message.obtain(null, messageType, 0, 0, data);
		msg.replyTo = incomingMessenger;
		Log.d(TAG, "Sending " + data.toString());
		try
		{
				messengerService.send(msg);
		} 
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void sendMessageToService(final int messageType, Object item)
	{
		Log.d(TAG, "Sending message to service: " + messageType);
		final Message msg = Message.obtain(null, messageType, item);
		msg.replyTo = incomingMessenger;
		try
		{
			if(messengerService!=null)
			{
				messengerService.send(msg);
			}
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//new SendMessageTask().execute(msg);
	}


	private class SendMessageTask extends AsyncTask<Message, Void, Void>
	{
		@Override
		protected synchronized Void doInBackground(final Message... msgs)
		{
			try
			{
				for (final Message msg : msgs)
				{
					try
					{
						Log.d(TAG, "Sending message to service:" + msg.what);
						messengerService.send(msg);
					} 
					catch (final RemoteException e)
					{
						Log.e(TAG,
								"Exception when sending message from autoservice activity");
						Log.d(TAG, e.getMessage());
					}

				}
			}
			catch (final NullPointerException e)
			{
				Log.d(TAG, "Messages to send was null");
			}
			return null;
		}
	}

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler
	{
		@Override
		public void handleMessage(final Message msg)
		{
			Log.d(TAG, "Incoming message on Activity:" + msg.what);
		}
	}

}


