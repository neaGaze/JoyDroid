package com.android.joydroid.client.app;

import java.io.IOException;
import java.util.HashSet;
import com.android.joydroid.R;
import com.android.joydroid.client.connection.Connection;
import com.android.joydroid.client.connection.ConnectionList;
import com.android.joydroid.client.protocol.JoydroidActionReceiver;
import com.android.joydroid.client.protocol.JoydroidAction;
import com.android.joydroid.client.protocol.AuthenticationAction;
import com.android.joydroid.client.protocol.AuthenticationResponseAction;
import com.android.joydroid.client.protocol.JoydroidConnection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Joydroid extends Application implements Runnable
{
	private static final long CONNECTION_CLOSE_DELAY = 3000;
	
	/**
	 * @uml.property name="preferences"
	 */
	private SharedPreferences preferences;
	private Vibrator vibrator;
	
	/**
	 * @uml.property name="connection"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 */
	private JoydroidConnection[] connection;		/* here JoydroidConnection is a abstract class and yet it has been used to create an object. In this case that 
	be the reason why array has been used to create the reference to the class JoydroidConnection*/
	private HashSet<JoydroidActionReceiver> actionReceivers;
	
	private Handler handler;
	
	/**
	 * @uml.property name="closeConnectionScheduler"
	 * @uml.associationEnd
	 */
	public CloseConnectionScheduler closeConnectionScheduler;		//This class has been declared at the end of this file to stop the  ActionReceivers from recieving further actions
	
	/**
	 * @uml.property name="connections"
	 * @uml.associationEnd
	 */
	private ConnectionList connections;
	
	public void onCreate()
	{
		super.onCreate();
		
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
		this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		
//		this.actionReceivers = new HashSet<JoydroidActionReceiver>();    //This is not required here we don't have any action to listen in the android application
		
		this.handler = new Handler();			//used to manually put the string in the toast instead of the R file
		
		this.connection = new JoydroidConnection[1];
		
		this.closeConnectionScheduler = new CloseConnectionScheduler();
		
		this.connections = new ConnectionList(this.preferences);
	}
	
	/**
	 * @return
	 * @uml.property name="preferences"
	 */
	public SharedPreferences getPreferences()
	{
		return this.preferences;
	}
	
	/**
	 * @return
	 * @uml.property name="connections"
	 */
	public ConnectionList getConnections()
	{
		return this.connections;
	}
	
	public void vibrate(long l)
	{
		if (this.preferences.getBoolean("feedback_vibration", true))
		{
			this.vibrator.vibrate(l);
		}
	}
	
	public float getSensitivity()
	{
//		Float a=this.preferences.getFloat("text_accelerometer_sensitivity", (float) 0.7);
		float a=Float.parseFloat(this.preferences.getString("text_accelerometer_sensitivity", null));
		
		return a;
	}
	
	public String getMapping(String st)
	{
		return (this.preferences.getString(st, null));
	}
	
	public boolean getSensor()
	{
		//sensor disable false is equivalent to sensor enable true 
		if(this.preferences.getBoolean("disable_sensor", false))
		{
				//create a sensor object that through a proxy object
				return true;
		}
		else return false;
	}
	
	/* This function is called for Connection establishment, password verification and processing inputStream */
	public synchronized void run()
	{
		Connection co = this.connections.getUsed();  
		if (co != null)
		{  
			JoydroidConnection c = null;
			/* Here "Connection" is somewhat llke connection information and "JoydroidConnection" is somewhat like using those connection informatin to connect to the network */
			try
			{   	
				c = co.connect(this);	//establishes socket connection. See ConnectionWifi.java and ConnectionTcp
			
				synchronized (this.connection)
				{	
					this.connection[0] = c;   
					Log.i("SUCCESS IN CONNECTION", " connected to the network");
				}
				
				try
				{
					this.showMyAppToast(R.string.text_connection_established);	/* succesful establishment of connection */
					Log.i(" CONNECTION Establsihed", "Connection established");
					String password = co.getPassword();			/* check for password correctness */
					this.sendAction(new AuthenticationAction(password));				
					
					while (true) /* if password is true, then decrypt inputStream to read wha action is requested to occur */
					{
						JoydroidAction action = c.receiveAction();
						Log.v("OK, Actions received",":)");
						this.receiveAction(action);
					}
				}
				finally
				{
					synchronized (this.connection)
					{
						this.connection[0] = null;
					}
					
					c.close(); 			Log.i(" STOPPED CONNECTION?", "stop connection");
				}
			}
			catch (IOException e)
			{
				if (c == null)
				{
					this.showMyAppToast(R.string.text_connection_refused);
					
				}
				else
				{
					this.showMyAppToast(R.string.text_connection_closed);
				}
			}
			catch (IllegalArgumentException e)
			{
				this.showMyAppToast(R.string.text_illegal_connection_parameter);
			}
		}
		else
		{	
		//	this.showMyAppToast(R.string.text_no_connection_selected); 	
			this.showMyAppToast("OK, connection is already null");
		}
	}
	
	public void sendAction(JoydroidAction action)
	{
		synchronized (this.connection)
		{
			if (this.connection[0] != null)
			{
				try
				{
					this.connection[0].sendAction(action);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	// overload vako, 2 typeko argument lina sakchha either resource id(string)
	// or string
	public void showMyAppToast(int resId)
	{
	//	if (this.isInternalToast())
		{
			this.showToast(resId);
		}
	}
	
	public void showMyAppToast(String message)
	{
	//	if (this.isInternalToast())
		{
			this.showToast(message);
		}
	}
	
	public boolean isInternalToast()
	{
		synchronized (this.actionReceivers)
		{
			return !this.actionReceivers.isEmpty();
		}
	}
	
	/* show toast by Resource xml file */
	public void showToast(int resId)
	{
		this.showToast(this.getResources().getString(resId));
	}
	
	/* show toast by writing the message itself into the toast */
	public void showToast(final String message)
	{
		this.handler.post(new Runnable()
		{
			public void run()
			{
				Toast.makeText(Joydroid.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void receiveAction(JoydroidAction action)
	{
	/*	synchronized (this.actionReceivers)
		{
			for (JoydroidActionReceiver actionReceiver : this.actionReceivers)		//for processing all the actions in the ActionReceivers
			{
				actionReceiver.receiveAction(action);
			}
		}
	*/	
		if (action instanceof AuthenticationResponseAction)
		{
			this.receiveAuthenticationResponseAction((AuthenticationResponseAction) action);
		}
	}
	
	private void receiveAuthenticationResponseAction(AuthenticationResponseAction action)
	{
		if (action.authenticated)
		{
			this.showMyAppToast(R.string.text_authenticated);		//Login succesful
		}
		else
		{
			this.showMyAppToast(R.string.text_not_authenticated);		//Wrong Password
		}
	}
	
	//called by "JoydroidActivity.java"  for creating the buffers of actions to be received into the android application
	public void registerActionReceiver()
	{
		synchronized(this.connection)
		{
			if(this.connection[0] == null)
			{
				this.showMyAppToast(R.string.text_no_connection_selected);    //No connection selected\nPress the \"Menu\" button\nSelect \"Connections\"
				(new Thread(this)).start();
			}
		}
	}
	
	//    This is commented because it receives JoydroidActionReceiver as a parameter, which in our case is not required
	 	public void registerActionReceiver(JoydroidActionReceiver actionReceiver)
	{
		synchronized (this.actionReceivers)
		{
			this.actionReceivers.add(actionReceiver);
			
			if (this.actionReceivers.size() > 0)
			{
				synchronized (this.connection)
				{
					if (this.connection[0] == null)
					{
						(new Thread(this)).start();			//thread starts and runs "run()"
					}
				}
			}
		}
	}   

	public void unregisterActionReceiver()
	{
		this.closeConnectionScheduler.schedule();
	}
	
	public void unregisterActionReceiver(JoydroidActionReceiver actionReceiver)
	{
		synchronized (this.actionReceivers)
		{
			this.actionReceivers.remove(actionReceiver);
			
			if (this.actionReceivers.size() == 0)
			{
				this.closeConnectionScheduler.schedule();
			}
		}
	}  
	
	public class CloseConnectionScheduler implements Runnable
	{
		private Thread currentThread;
		
		public synchronized void run()
		{
			try
			{
				this.wait(Joydroid.CONNECTION_CLOSE_DELAY);
				
		/*		synchronized (Joydroid.this.actionReceivers)
				{
					if (Joydroid.this.actionReceivers.size() == 0)
					{      */
						synchronized (Joydroid.this.connection)
						{    
							if (Joydroid.this.connection[0] != null)
							{
								Joydroid.this.connection[0].close();
								
								Joydroid.this.connection[0] = null;
							}
						}
		/*			}
				}    */
				
				this.currentThread = null;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public synchronized void schedule()
		{
			if (this.currentThread != null)
			{
				this.currentThread.interrupt();
			}
			
			this.currentThread = new Thread(this);
			
			this.currentThread.start();
		}
	}
}
