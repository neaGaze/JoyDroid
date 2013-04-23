package com.android.joydroid.client.connection;

import java.io.IOException;
import java.io.Serializable;

import com.android.joydroid.client.app.Joydroid;
import com.android.joydroid.client.connection.activity.ConnEditActivity;
import com.android.joydroid.client.protocol.JoydroidConnection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/* This is used to manipulate the name and password of the connection. the port and the host is manipulated in ConnectionWifi */
public abstract class Connection implements Comparable<Connection>, Serializable
{
	/**
	 * @uml.property name="name"
	 */
	private String name;
	/**
	 * @uml.property name="password"
	 */
	private String password;
	
	// default constructor
	public Connection()
	{
		this.name = "";
		this.password = "qwerty";
	}
	
	/* load the certain name and password from shared preferences  with respective position*/
	public static Connection load(SharedPreferences preferences, ConnectionList list, int position)
	{
		Connection connection = null;
		connection = ConnectionWifi.load(preferences, position);	/* ConnectionWifi is extended from this class so it can also return Connection instead of ConnectionWifi */
		
		connection.name = preferences.getString("connection_" + position + "_name", null);
		connection.password = preferences.getString("connection_" + position + "_password", null);
		
		return connection;
	}
	
	/* save the name and password in the saved preferences with the names: Eg:- connection_0_name represent the first name of the preferences*/
	public void save(Editor editor, int position)
	{
		editor.putString("connection_" + position + "_name", this.name);
		editor.putString("connection_" + position + "_password", this.password);
	}
	
	/* for function overriding of ConnectionWifi class */
	public abstract JoydroidConnection connect(Joydroid application) throws IOException;
	
	public abstract void edit(Context context);
	
	protected void edit(Context context, Intent intent)
	{
		ConnEditActivity.connectionParam = this;
		context.startActivity(intent);
	}
	
	/**
	 * @return
	 * @uml.property name="name"
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param name
	 * @uml.property name="name"
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return
	 * @uml.property name="password"
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * @param password
	 * @uml.property name="password"
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public int compareTo(Connection c)
	{
		return this.name.compareTo(c.name);
	}
}
