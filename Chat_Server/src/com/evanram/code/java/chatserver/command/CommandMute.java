package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.SessionWorker;
import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

public class CommandMute implements RunCommand
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
		if(commander.isOp())
		{
			if(args.length > 0)
			{
				for(SessionWorker w : Server.getSessionWorkers())
				{
					if(w.getName().equalsIgnoreCase(args[0]))
					{
						if(w.isSilenced())
						{
							w.setSilenced(false);
							w.sendMessage("You are no longer muted.");
							commander.sendMessage("Un-muted " + w.getName());
						}
						else
						{
							w.setSilenced(true);
							w.sendMessage("You have been muted.");
							commander.sendMessage("Muted " + w.getName());
						}
						
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
		}
		else
		{
			commander.sendMessage("You must be an operator!");
		}
		return true;
	}

}
