package com.android.joydroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentActivityModule extends Fragment
{
	private JoyDroidActivity.SimulationView simView;
	public FragmentActivityModule(JoyDroidActivity.SimulationView  _simView)
	{
		Log.i("FRAGMENTATION_LOG","FragmentActivity()");
		simView= _simView;
	}
	
	void setView(JoyDroidActivity.SimulationView _simView)
	{
		simView= _simView;
	}
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	
		return (View)simView;
	 }
}		