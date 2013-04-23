package com.android.joydroid.client.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//equivalent to keyboardAction in RemotePCDroid

public class ButtonAction extends JoydroidAction
{
	public static  int BUT_X=119;
	public static  int BUT_A =120;
	public static  int BUT_B =121;
	public static  int BUT_Y =122;
	
	public static final int UNICODE_BACKSPACE = -1;
	private int but;
	
	public ButtonAction(int but)
	{
		this.but=but;
	}

	public static  void changeButtonValue(int newButX, int newButY, int newButA, int newButB)
	{
		BUT_X = newButX;
		BUT_Y = newButY;
		BUT_A = newButA;
		BUT_B = newButB;
	}
	@Override
	public void toDataOutputStream(DataOutputStream dos) throws IOException {
		dos.writeByte(BUTTON );
		dos.writeInt(but);
		
	}
	
	public static ButtonAction parse(DataInputStream dis) throws IOException
	{
		int but = dis.readInt();
		return new ButtonAction(but);
	}
}