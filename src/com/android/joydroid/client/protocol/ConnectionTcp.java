package com.android.joydroid.client.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.util.Log;
//import com.android.joydroid.protocol.connection.Connection;

public class ConnectionTcp extends JoydroidConnection
{
	public final static int DEFAULT_PORT = 6666;
	
	private Socket socket;
	
	public ConnectionTcp(Socket socket) throws IOException
	{
		super(socket.getInputStream(), socket.getOutputStream());
		
		this.socket = socket;
		this.socket.setPerformancePreferences(0, 2, 1);
		this.socket.setTcpNoDelay(true);
		this.socket.setReceiveBufferSize(1024 * 1024);
		this.socket.setSendBufferSize(1024 * 1024);
	}
	
	public static ConnectionTcp create(String host, int port) throws IOException
	{   

		Log.v("host:"+ host+" port: "+ port, "check this");
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(host, port), 1000);   //error in here 
		Log.v("Another CAll successful", "then what is wrong");
		ConnectionTcp connection = new ConnectionTcp(socket);
		return connection;
	}
	
	public InetAddress getInetAddress()
	{
		return this.socket.getInetAddress();
	}
	
	public int getPort()
	{
		return this.socket.getPort();
	}
	
	public void close() throws IOException
	{
		this.socket.shutdownInput();
		this.socket.shutdownOutput();
		super.close();
		this.socket.close();
	}
}
