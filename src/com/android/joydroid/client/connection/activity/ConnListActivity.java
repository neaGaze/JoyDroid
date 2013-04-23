package com.android.joydroid.client.connection.activity;

import com.android.joydroid.R;
import com.android.joydroid.client.app.Joydroid;
import com.android.joydroid.client.connection.Connection;
import com.android.joydroid.client.connection.ConnectionList;
import com.android.joydroid.client.connection.ConnectionWifi;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;


/* This is module implementation of ConnectionList.java in android activity */
public class ConnListActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener, OnClickListener
{
	/**
	 * @uml.property name="application"
	 * @uml.associationEnd
	 */
	private Joydroid application;
	
	/**
	 * @uml.property name="connections"
	 * @uml.associationEnd
	 */
	private ConnectionList connections;
	
	/**
	 * @uml.property name="adapter"
	 * @uml.associationEnd
	 */
	private ConnectionListAdapter adapter;		//defined later in this file
	
	private AlertDialog alertDialogNew;     		//shows the alert box saying "Add New Server"
	
	private AlertDialog alertDialogItemLongClick;		//for the alert box saying " Use ", "Edit" and "Remove"
	
	private int itemLongClickPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (Joydroid) this.getApplication();
		this.connections = this.application.getConnections();   	//call to the joyDroid.java's getConnections()
		
		this.adapter = new ConnectionListAdapter(this, this.connections);  	/*pass to the constructor ConnectionListAdapter(Context, ConnectionList). ListActivity is the extension
		 of context, so "this" or listActivity can also be passed to the Context class */
		this.setListAdapter(this.adapter);
		
		this.getListView().setOnItemClickListener(this);
		
		this.getListView().setOnItemLongClickListener(this);
		
		this.init();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		this.refresh();
		
		if (this.connections.getCount() == 0)
		{
			this.application.showToast(R.string.text_no_connection);
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		this.connections.save();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.text_new);		//text_new==New Connection
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case 0:
				this.alertDialogNew.show();			//when "New Connection" is selected, alertbOx is shown telling "Add new server"
				break;
		}
		
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)     // onClicking the any one of the saved connection servers
	{
		this.useConnection(position);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)   // on Long Clicking any one of the saved connection servers
	{
		this.itemLongClickPosition = position;
		
		this.alertDialogItemLongClick.show();		//shows the alert box saying " Use ", "Edit" and "Remove"
		
		return true;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if (dialog == this.alertDialogNew)
		{
			this.addConnection(which);			// if "alertDialogNew" is selected or clicked for a small time then new connection is added			
		}
		else if (dialog == this.alertDialogItemLongClick)
		{
			this.menu(which);				// if "alertDialogitemLongClick" is selected or clicked for a long time then see just below
		}
	}
	
	/*
	 * select what happens when the item in the menu is touched for a long
	 * peroid of time
	 */
	private void menu(int which)
	{
		Connection connection = this.connections.get(this.itemLongClickPosition);	// call to the "get(position)" of "ConnectionList" class returning "Connection"
		
		switch (which)
		{
			case 0:						// When "Use" is selected
				this.useConnection(this.itemLongClickPosition);
				break;
			case 1:						// When "Edit" is selected
				connection.edit(this);
				break;
			case 2:						// When "Remove" is selected
				this.removeConnection();
				break;
		}
	}
	
	// Called when "alertDialogNew" is selected or "Add new Server" is selected
	private void addConnection(int which)
	{	
		Connection connection = this.connections.add(); //call to the ConnectionList.add() returning a connection object				
		this.refresh();		
		connection.edit(this);
	}
	
	private void useConnection(int position)
	{
		this.connections.use(position);
		this.refresh();
	}
	
	private void removeConnection()
	{
		this.connections.remove(this.itemLongClickPosition);
		this.refresh();
	}
	
	private void refresh()
	{
		this.connections.sort();
		this.adapter.notifyDataSetChanged();
	}
	
	private void init()
	{
		this.initAlertDialogNew();
		
		this.initAlertDialogItemLongClick();
	}
	
	private void initAlertDialogNew()
	{
		String[] connectionTypeName = {
			this.getResources().getString(R.string.text_wifi) /* text_wifi==Add new server */
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Wi-Fi Server");
		builder.setItems(connectionTypeName, this);
		this.alertDialogNew = builder.create();
	}
	
	private void initAlertDialogItemLongClick()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(R.array.connection_action_name, this); 	//aonnection_action-name==use, Edit or  Remove
		this.alertDialogItemLongClick = builder.create();
	}
	
	/**
	 * @author samar
	 */
	/* used to change the data to the view */
	private class ConnectionListAdapter extends BaseAdapter
	{
		/**
		 * @uml.property name="connections"
		 * @uml.associationEnd
		 */
		private ConnectionList connections;
		private LayoutInflater layoutInflater;
		
		private int connectionUsedPosition;
		
		public ConnectionListAdapter(Context context, ConnectionList connections)
		{
			this.connections = connections;
			
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			this.connectionUsedPosition = this.connections.getUsedPosition();
		}
		
		@Override
		public void notifyDataSetChanged()	/*Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.*/
		{
			super.notifyDataSetChanged();
			
			this.connectionUsedPosition = this.connections.getUsedPosition();
		}
		
		@Override
		public int getCount()		// override AdapterView's getCount() function
		{
			return this.connections.getCount();	//call to connectionList's getCount()
		}
		
		@Override
		public Connection getItem(int position)
		{
			return this.connections.get(position);
		}
		
		@Override
		public long getItemId(int position)
		{
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)     // conert the data into the view
		{
			ConnectionViewHolder holder;
			
			if (convertView == null)/*If convertView == null, then the method needs to create a new View object 
				and return that, an expensive operation*/
			{
				holder = new ConnectionViewHolder();
				
				convertView = this.layoutInflater.inflate(R.layout.connection, null);
				
				holder.use = (RadioButton) convertView.findViewById(R.id.use);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ConnectionViewHolder) convertView.getTag();
			}
			
			Connection connection = this.connections.get(position);
			
			holder.use.setChecked(position == this.connectionUsedPosition);
			
			if (connection instanceof ConnectionWifi)
			{
				holder.icon.setImageResource(R.drawable.wifi);
			}
			
			holder.name.setText(connection.getName());
			
			return convertView;
		}
		
		private class ConnectionViewHolder
		{
			public RadioButton use;
			public ImageView icon;
			public TextView name;
		}
	}
}
