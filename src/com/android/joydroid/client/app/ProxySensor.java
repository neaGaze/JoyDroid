package com.android.joydroid.client.app;

import com.android.joydroid.JoyDroidActivity;
import com.android.joydroid.client.app.Joydroid;
import com.android.joydroid.client.protocol.DirectionControlAction;

public class ProxySensor {
	
	private boolean flag;
	private Joydroid apps;
	
	public ProxySensor(Joydroid apps)
	{
		this.apps = apps;
	}
	
	public void sendViaProxy(int acclValue)
	{
		flag = this.apps.getSensor(); //if sensor enable found perform following opearations
		if(flag)
		{
			//send Action of directional control
  			
  			this.apps.sendAction(new DirectionControlAction(acclValue ));   
		}
	}
}
