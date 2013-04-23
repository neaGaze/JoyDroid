package com.android.joydroid.client.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class DirectionControlAction extends JoydroidAction
{
	public static final int DIR_UP_LEFT = 0;
	public static final int DIR_UP = 38;
	public static final int DIR_UP_RIGHT = 1;
	public static final int DIR_LEFT = 37;
	public static final int DIR_CENTER = 2;
	public static final int DIR_RIGHT = 39;
	public static final int DIR_DOWN_LEFT = 3;
	public static final int DIR_DOWN = 40;
	public static final int DIR_DOWN_RIGHT = 4;
	
	public int direction;
	
	public DirectionControlAction(int direction)
	{
		this.direction = direction;
	}
	
	@Override
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(DIRECTION);
		dos.writeInt(direction);
		
	}
	
	public static DirectionControlAction parse(DataInputStream dis) throws IOException
	{
		int direction = dis.readInt();
		return new DirectionControlAction(direction);
	}
}