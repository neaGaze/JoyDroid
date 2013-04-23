package com.android.joydroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.joydroid.client.app.*;
import com.android.joydroid.client.protocol.ButtonAction;
import com.android.joydroid.R;

public class PreferencesActivity extends PreferenceActivity
{
	int a,tmpIncr=0;
	int[] butVal = new int[4];
	private Joydroid application;
	private SharedPreferences preferences ;
	private static final String[] tabFloatPreferences = {
        "text_accelerometer_sensitivity"
};
	
	private static final String[] tabIntPreferences = {
        "map_x", "map_y","map_a","map_b"
};
	private static final int resetPreferencesMenuItemId = 0;
	 
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.addPreferencesFromResource(R.xml.preferences);
	        this.application=(Joydroid) this.getApplication();
	       this.preferences=this.application.getPreferences();
	        // this.preferences= PreferenceManager.getDefaultSharedPreferences(this);
	 }
	 
	 protected void onPause()
		{
			super.onPause();
			this.checkPreferences();
			this.setButtonValues();
		}
	 
	 private void setButtonValues() {
		// TODO Auto-generated method stub
		 for(String tmpStr : tabIntPreferences)
		 {
		 	String m=application.getMapping(tmpStr);     		
    		int i = m.charAt(0);
    		Log.i("PARSE  SUCCESFULL","IT's "+m+"=="+i);
    		butVal[tmpIncr] = i;
    		tmpIncr++;
		 }
    		ButtonAction.changeButtonValue(butVal[0], butVal[1], butVal[2], butVal[3]);
    //		Log.i("New Button values:",butVal[0]+" "+butVal[1]+" "+butVal[2]+" "+butVal[3]);
	}

	public boolean onCreateOptionsMenu(Menu menu)
		{
			menu.add(Menu.NONE, resetPreferencesMenuItemId, Menu.NONE, this.getResources().getString(R.string.text_preferences));
		
			return super.onCreateOptionsMenu(menu);
		}
		
		public boolean onOptionsItemSelected(MenuItem item)
		{
			switch (item.getItemId())
			{
				case resetPreferencesMenuItemId:
					this.resetPreferences();
					break;
			}
			
			return super.onOptionsItemSelected(item);
		}
	 
	 private void checkPreferences()
	 {

			Editor editor = this.preferences.edit();
			
			for (String s : tabFloatPreferences)
			{
				try
				{
					Float.parseFloat(this.preferences.getString(s, null));
					Log.i("Value for Float","  This is it");
				}
				catch (NumberFormatException e)
				{
					// this.application.debug(e);
					editor.remove(s);
				}
			}
			

			for (String s1 : tabIntPreferences)
			{
				try
				{
			//		a = Integer.parseInt(this.preferences.getString(s1, null));
			//		Log.i("Value is: "+a,"  This is it");
				
					String str= this.preferences.getString(s1, null);
					Log.i("Value is: "+str,"  This is it");
				}
				catch (NumberFormatException e)
				{
					// this.application.debug(e);
					editor.remove(s1);
					Log.i("exception caught","NumberException it is");
				}
			}
			
			
			editor.commit();
			
			PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
	 }
	 
	 private void resetPreferences()
		{
			this.setPreferenceScreen(null);
			
			Editor editor = this.preferences.edit();
			editor.clear();
			editor.commit();
			
			PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
			
			this.addPreferencesFromResource(R.xml.preferences);
		}
}