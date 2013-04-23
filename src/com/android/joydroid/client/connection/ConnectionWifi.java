package com.android.joydroid.client.connection;

import java.io.IOException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.android.joydroid.client.protocol.ConnectionTcp;
import com.android.joydroid.client.protocol.JoydroidConnection;
import com.android.joydroid.client.app.Joydroid;
import com.android.joydroid.client.connection.activity.ConnWifiEditActivity;
/*******
 * used to manipulate the port and ip (host) addresses
 **********/
public class ConnectionWifi extends Connection
{
	/**
	 * @uml.property name="host"
	 */
	private String host;
	/**
	 * @uml.property name="port"
	 */
	private int port;
	
	public ConnectionWifi()
	{
		super(); // automaticallly calls the constructor of Connection class
		
		this.host = "";
		this.port = ConnectionTcp.DEFAULT_PORT;
	}
	
	/*
	 * load the host and port field only by the saved preferences with
	 * respective position
	 */
	public static ConnectionWifi load(SharedPreferences preferences, int position)
	{
		ConnectionWifi connection = new ConnectionWifi();
		
		connection.host = preferences.getString("connection_" + position + "_host", null);
		
		connection.port = preferences.getInt("connection_" + position + "_port", 0);
		
		return connection;
	}
	
	/*
	 * save the port and host(ip) in the saved preferences with the names: Eg:-
	 * connection_0_host represent the first host in the preferences
	 */
	public void save(Editor editor, int position)
	{
		super.save(editor, position);
		editor.putString("connection_" + position + "_host", this.host);
		editor.putInt("connection_" + position + "_port", this.port);
	}
	
	public void edit(Context context)
	{
		Intent intent = new Intent(context, ConnWifiEditActivity.class);
		this.edit(context, intent);
	}
	
	public JoydroidConnection connect(Joydroid application) throws IOException
	{  
		Log.v("CONNECTIONWIFI CORRECT host:"+this.host+" port:"+this.port, "Call Successful");
		return ConnectionTcp.create(this.host, this.port);
	}
	
	/**
	 * @return
	 * @uml.property name="host"
	 */
	public String getHost()
	{
		return host;
	}
	
	/**
	 * @param host
	 * @uml.property name="host"
	 */
	public void setHost(String host)
	{
		this.host = host;
	}
	
	/**
	 * @return
	 * @uml.property name="port"
	 */
	public int getPort()
	{
		return port;
	}
	
	/**
	 * @param port
	 * @uml.property name="port"
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
}
