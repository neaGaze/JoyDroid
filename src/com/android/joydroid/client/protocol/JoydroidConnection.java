package com.android.joydroid.client.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import com.android.joydroid.client.protocol.JoydroidAction;;
//import com.android.joydroid.protocol.action.*;

/* Purpose of this class is to receive and send data also this class is used only for extending and not to create object. This class is exended by RemotePCDroidConnectionTcp.java */
public abstract class JoydroidConnection
{
	public static final String DEFAULT_PASSWORD = "qwerty";
	
	private DataInputStream dataInputStream;
	private OutputStream outputStream;
	
	public JoydroidConnection(InputStream inputStream, OutputStream outputStream)
	{
		this.dataInputStream = new DataInputStream(inputStream);
		this.outputStream = outputStream;
	}

	public JoydroidAction receiveAction() throws IOException
	{
		synchronized (this.dataInputStream)
		{
			JoydroidAction action = JoydroidAction.parse(this.dataInputStream);
			Log.i("ACTION RECEIVED","Action is received succesfully at client");
			return action;
		}
	}
	
	public void sendAction(JoydroidAction action) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		action.toDataOutputStream(new DataOutputStream(baos));		//wrote the action type and action int
		
		synchronized (this.outputStream)
		{
			this.outputStream.write(baos.toByteArray());
			this.outputStream.flush();
			Log.v("Action sent","Outputstream written and flushed ");
		}
	}
	
	public void close() throws IOException
	{
		this.dataInputStream.close();
		this.outputStream.close();
	}
}
