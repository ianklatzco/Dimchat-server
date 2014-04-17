package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

/**
 * Command to get the server's message of the day (MOTD).
 */
public class CommandMotd implements RunCommand
{

	@Override
	public String[] aliases()
	{
		return null;
	}

	@Override
	public String description()
	{
		return "Read the Server's MOTD (Message of the Day).";
	}
	
	@Override
	public String usage()
	{
		return "!motd";
	}

	@Override
	public boolean execute(Commander commander, String[] args)
	{
		commander.sendMessage(Server.getMotd());
		return true;
	}

}
