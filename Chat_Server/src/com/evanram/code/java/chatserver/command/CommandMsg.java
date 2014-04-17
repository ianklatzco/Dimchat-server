package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.SessionWorker;
import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

public class CommandMsg implements RunCommand
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
		if(args.length > 1)
		{
			for(SessionWorker w : Server.getSessionWorkers())
			{
				if(w.getName().equalsIgnoreCase(args[0]))
				{
					String message = "";
					
					for(int i = 1; i<args.length; i++)
					{
						message = message + " " + args[i];
					}
					String prefix = "[" + commander.getName() + " -> " + w.getName() + "] ";
					w.sendMessage(prefix + message);
					return true;
				}
			}
			
			commander.sendMessage("That name could not be found. Use \'!list\' to see client names.");
		}
		else
		{
			commander.sendMessage("Not enough arguments!");
			return false;
		}
		
		return true;
	}

}
