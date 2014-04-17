package com.evanram.code.java.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SessionWorker implements Runnable, Commander
{
	private Socket client;
	private String ip;
	private String name = "anonymous";
	private PrintWriter clientOut;
	private String previousMessage = "";
	private long previousMessageTime = 0L;
	private int messageTimelimitMillisec = 500;
	private int spamWarnCount = 1;
	private boolean silenced = false;
	
	/**
	 * SessionWorker constructor, takes a Socket input.
	 * Every client that connects has a SessionWorker (run in a separate Thread).
	 * @param client
	 */
	public SessionWorker(Socket client)
	{
		this.client = client;
		this.ip = client.getInetAddress().toString();
	}
	
	/**
	 * Get the Socket of this client.
	 * @return
	 */
	public Socket getClient()
	{
		return client;
	}
	
	/**
	 * Formats SessionWorker: "[ip:port (name)]"
	 */
	@Override
	public String toString()
	{
		return "[" + getIp() + ":" + getPort() + "(" + getName() + ")]";
	}
	
	/**
	 * Get the IP of this client.
	 * @return
	 */
	public String getIp()
	{
		return getClient().getInetAddress().toString();
	}
	
	/**
	 * Get the port of this client.
	 * @return
	 */
	public int getPort()
	{
		return getClient().getPort();
	}
	
	/**
	 * Send a message to this client's client application (no one else sees this).
	 */
	public void sendMessage(String message)
	{
		clientOut.println(message);
	}
	
	/**
	 * Assigns a nickname to this client.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Get the amount of time that must be waited before sending a new message.
	 * @return
	 */
	public int getMessageTimelimitMillisec()
	{
		return messageTimelimitMillisec;
	}
	
	/**
	 * Gets the nickname of this client.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Return true if the list of operator IPs in Server contains this client's IP.
	 */
	public boolean isOp()
	{
		return Server.getOperatorIps().contains(getClient().getInetAddress().toString());
	}
	
	/**
	 * Get if the client is silenced (muted) or not.
	 * @return
	 */
	public boolean isSilenced()
	{
		return silenced;
	}
	
	/**
	 * Set the silence (mute) mode of this client.
	 * @param mode
	 */
	public void setSilenced(boolean mode)
	{
		silenced = mode;
	}
	
	/**
	 * Return true if the list of banned IPs in Server contains this client's IP.
	 */
	public boolean isBlacklisted()
	{
		return Server.getBlacklistedIps().contains(getClient().getInetAddress().toString());
	}
	
	/**
	 * Try to detect possible incoming spam from the client.
	 * Operators are expempt from the spam check.
	 * @param currentMessage
	 * @param previousMessage
	 * @return
	 */
	public boolean mightBeSpam(String currentMessage, String previousMessage)
	{
		//operators get exempt from this
		if(isOp())
		{
			return false;
		}
		
		//checks if the message time limit has expired
		boolean timelimitExpired = System.currentTimeMillis() - previousMessageTime >= messageTimelimitMillisec;
		
		//timelimit for duplicate messages (3 times the messageTimelimitMillisec)
		boolean duplicateTimelimitExpired = System.currentTimeMillis() - previousMessageTime >= 3*messageTimelimitMillisec;
		
		//block sending messages too fast
		if(!timelimitExpired)
		{
			return true;
		}
		
		//block duplicate messages if they're sent within timelimit of each other
		if(currentMessage.equalsIgnoreCase(previousMessage) && !duplicateTimelimitExpired)
		{
			return true;
		}
		
		//block all-caps messages (of length 10 or more)
		else if(currentMessage.toUpperCase().equals(currentMessage) && currentMessage.length() >= 10)
		{
			return true;
		}
		
		//block empty messages
		else if(currentMessage.replace(" ", "").equals(""))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Constantly check for new messages and handle client's input to the server.
	 */
	public void run()
	{
		BufferedReader in = null;
		String line;
		
		try
		{
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			clientOut = new PrintWriter(client.getOutputStream(), true);
		}
		catch(IOException e)
		{
			Message.logError("Encountered IOException when assigning in|out read-write");
			System.exit(-1);
		}
		
		while(true)
		{
			try
			{
				line = in.readLine();
				
				if(line == null)	//notify that this client is disconnected
				{
					Server.getSessionWorkers().remove(this);
					Message.broadcast(this.getName() + " has disconnected.");
					break;
				}
				else if((mightBeSpam(line, this.previousMessage) || isSilenced()) && !line.startsWith("!"))	//disrupt spam messages
				{
					if(isSilenced())
					{
						sendMessage("You cannot speak at this time!");
						continue;
					}
					
					if(this.spamWarnCount <= 0)
						sendMessage("Spam guard blocked potential spam message!");
					if(this.spamWarnCount >= 5)
						this.spamWarnCount = 0;
					
					previousMessage = line;
					previousMessageTime = System.currentTimeMillis();
					this.spamWarnCount++;

					continue;
				}
				
				previousMessage = line.startsWith("!") ? previousMessage : line;
				previousMessageTime = System.currentTimeMillis();
				
				//send string to THIS client
				String message = getName() + ": " + line;
				
				if(line.startsWith("!"))
				{
					new Command(this, line.substring(1));
				}
				
				for(Socket s : Server.getConnections())	//send to ALL clients
				{
					//don't send the client his message twice, and don't send others commands
					if(line.startsWith("!") || s.equals(client))
						continue;
					
					PrintWriter socketOutput = new PrintWriter(s.getOutputStream(), true);
					socketOutput.println(message);	//send client the message
				}
				
				//log string to server console
				Message.log(this.ip + ":" + this.client.getPort() + "(" + getName() + ")", line);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				Message.logError("Encountered IOException when reading input stream next line.");
				try
				{
					getClient().close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
				
				Thread.currentThread().interrupt();
				break;
			}
			
		}
	}
}
