package com.android.joydroid.client.connection;

import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class ConnectionList
{
	private ArrayList<Connection> connections;
	private SharedPreferences preferences;
	
	/**
	 * @uml.property  name="used"
	 * @uml.associationEnd  
	 */
	private Connection used;
	
	public ConnectionList(SharedPreferences preferences)
	{
		this.preferences = preferences;
		this.connections = new ArrayList<Connection>();
		
		this.load();
	}
	
	private void load()
	{
		int count = this.preferences.getInt("connection_count", 0);
		
		for (int i = 0; i < count; i++)
		{	// to load all the information about the saved connection in shared Preferences
			Connection connection = Connection.load(this.preferences, this, i);  //why is the load(...) called directly from the main class and not from object??
			this.connections.add(connection);		//add to the ArrayList about the connection
		}
		
		int position = this.preferences.getInt("connection_use", -1);
		if (position >= 0)
		{
			this.used = this.get(position);
		}
	}
	
	public void save()
	{
		Editor editor = this.preferences.edit();
		
		int count = this.connections.size();		/* save the size of the connection list in the count */
		editor.putInt("connection_count", count); /* put the "connection_count" with the number of lists of connection */
		
		editor.putInt("connection_use", this.getUsedPosition()); /* put the position integer in the string "connection_use" */
		
		for (int i = 0; i < count; i++)
		{
			this.connections.get(i).save(editor, i); // call to the save(Editor,position) of the ConnectionWifi class for all the array elements of the ArrayList
		}
		
		editor.commit();
	}
	
	public void sort()
	{
		Collections.sort(this.connections);
	}
	
	public Connection add()
	{
		Connection connection = null;
		connection = new ConnectionWifi();
		
		this.connections.add(connection);
		
		if (this.connections.size() == 1)
		{
			this.used = connection;
		}
		
		return connection;
	}
	
	/* reomves the connection from ArrayList of connections at the given position */
	public void remove(int position)
	{
		Connection connection = this.connections.remove(position);
		
		if (connection == this.used)
		{
			this.used = null;
		}
	}
	
	public Connection get(int position)
	{
		return this.connections.get(position); /* <ArrayList>.get(postition) returns the array element at the given position  which is the Connection itself*/
	}
	
	public int getCount()
	{
		return this.connections.size();  /* returns the size of the arraylist of connections */
	}
	
	public void use(int position)
	{
		this.used = this.get(position);  /* returns the position of the used connection among the list of connections in Arraylist */
	}
	
	/**
	 * @return
	 * @uml.property  name="used"
	 */
	public Connection getUsed()
	{
		return this.used;	/* returns the used connection among the Arraylist of connections */
	}
	
	public int getUsedPosition()
	{
		return this.connections.indexOf(this.used);
	}
}
