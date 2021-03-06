package com.android.joydroid.client.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthenticationAction extends JoydroidAction
{
	public String password;
	
	public AuthenticationAction(String password)
	{
		this.password = password;
	}
	
	public static AuthenticationAction parse(DataInputStream dis) throws IOException
	{
		String password = dis.readUTF();
		
		return new AuthenticationAction(password);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(AUTHENTICATION);
		dos.writeUTF(this.password);
	}
}
