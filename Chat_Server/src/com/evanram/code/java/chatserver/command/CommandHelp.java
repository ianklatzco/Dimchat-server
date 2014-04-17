package com.evanram.code.java.chatserver.command;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

public class CommandHelp implements RunCommand
{

	@Override
	public String[] aliases()
	{
		return null;
	}

	@Override
	public String description()
	{
		return null;
	}

	@Override
	public String usage()
	{
		return null;
	}

	@Override
	public boolean execute(Commander commander, String[] args)
	{
		ArrayList<String> validCommands = new ArrayList<String>();

		//get all commands on the server
		for (@SuppressWarnings("rawtypes") Entry e : Server.getRegisteredCommands().entrySet())
		{
			validCommands.add((String) e.getKey());
		}
		
		String formattedMessage = "Valid commands: ";
		for(String str : validCommands)
		{
			formattedMessage = formattedMessage + str + "  ";
		}

		commander.sendMessage(formattedMessage);
		return true;
	}

}
