package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.SessionWorker;
import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

/**
 * Get the list of all online clients (shows nicknames, not IP addresses).
 */
public class CommandList implements RunCommand
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
		String formattedMessage = "Online: ";
		for(SessionWorker w : Server.getSessionWorkers())
		{
			formattedMessage = formattedMessage + w.getName() + "  ";
		}

		commander.sendMessage(formattedMessage);
		
		return true;
	}

}
