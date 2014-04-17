package com.evanram.code.java.chatserver;

public class ServerCommand implements Commander
{
	private String message;
	private String name = "SERVER";
	
	public ServerCommand(String message)
	{
		this.message = message;
		nextCommand();
	}
	
	private void nextCommand()
	{
		while(true)
		{
			new Command(this, message);
			message = Message.nextInputLine();
		}
	}
	
	public boolean isOp()
	{
		return true;
	}
	
	public boolean isBlacklisted()
	{
		return false;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String newName)
	{
		name = newName;
	}
	
	public void sendMessage(String message)
	{
		Message.logInfo(message);
	}
}
