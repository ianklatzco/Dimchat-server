package com.evanram.code.java.chatserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Message
{
	/**
	 * Log a prefix with a message in the format "[prefix] message"
	 * @param prefix
	 * @param message
	 */
	public static void log(String prefix, String message)
	{
		log("[" + prefix + "] " + message);
	}
	
	/**
	 * Raw logs a message + a new line.
	 * @param message
	 */
	public static void log(String message)
	{
		logRaw(message + "\n");
	}
	
	/**
	 * Logs in the format "[!] message"
	 * @param message
	 */
	public static void logError(String message)
	{
		log("!", message);
	}
	
	/**
	 * Logs in the format "[i] message"
	 * @param message
	 */
	public static void logInfo(String message)
	{
		log("i", message);
	}
	
	/**
	 * Prints a message to sysout.
	 * @param message
	 */
	public static void logRaw(String message)
	{
		System.out.print(message);
	}
	
	/**
	 * Returns next System.in line from console.
	 * @return
	 */
	public static String nextInputLine()
	{
		Scanner sc = new Scanner(System.in);
		return sc.nextLine();
	}
	
	/**
	 * Broadcast a message to the server.
	 * @param message
	 */
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
