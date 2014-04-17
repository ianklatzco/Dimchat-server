package com.evanram.code.java.chatserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Message
{
	public static void log(String prefix, String message)
	{
		log("[" + prefix + "] " + message);
	}
	
	public static void log(String message)
	{
		logRaw(message + "\n");
	}
	
	public static void logError(String message)
	{
		log("!", message);
	}
	
	public static void logInfo(String message)
	{
		log("i", message);
	}
	
	public static void logRaw(String message)
	{
		System.out.print(message);
	}
	
	public static String nextInputLine()
	{
		Scanner sc = new Scanner(System.in);
		return sc.nextLine();
	}
	
	public static void broadcast(String message)
	{
		log("SERVER", message);

		for(Socket s : Server.getConnections())
		{
			try
			{
				PrintWriter socketOutput = new PrintWriter(s.getOutputStream(), true);
				socketOutput.println("[" + Server.getServerCommandInstance().getName() + "] " + message);	//send client the message
			}
			catch(IOException e)
			{
				logError("Encountered IOException with socket: "
							+ s.getInetAddress() + ":" + s.getPort());
			}
		}
	}
}
