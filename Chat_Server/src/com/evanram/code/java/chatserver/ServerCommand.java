package com.evanram.code.java.chatserver;

public class ServerCommand implements Commander
{
	private String message;
	private String name = "SERVER";
	
	/**
	 * ServerCommand constructor.
	 * The console sends commands through ServerCommand.
	 * @param message
	 */
	public ServerCommand(String message)
	{
		this.message = message;
		nextCommand();
	}
	
	/**
	 * Gets next command issused to server.
	 */
	private void nextCommand()
	{
		while(true)
		{
			new Command(this, message);
			message = Message.nextInputLine();
		}
	}
	
	/**
	 * ServerCommand is operator by default.
	 */
	public boolean isOp()
	{
		return true;
	}
	
	/**
	 * ServerCommand is not banned by default.
	 */
	public boolean isBlacklisted()
	{
		return false;
	}
	
	/**
	 * Get the nickname of ServerCommand.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Set the nickname of ServerCommand.
	 */
	public void setName(String newName)
	{
		name = newName;
	}
	
	/**
	 * Logs message to console.
	 */
	public void sendMessage(String message)
	{
		Message.logInfo(message);
	}
}
