package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.SessionWorker;
import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

/**
 * Command to change your nickname.
 */
public class CommandNick implements RunCommand
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
		if(commander instanceof SessionWorker)
		{
			SessionWorker worker = (SessionWorker)commander;
			if(args.length > 0)
			{
				for(SessionWorker w : Server.getSessionWorkers())
				{
					if(args[0].equalsIgnoreCase(w.getName()))
					{
						worker.sendMessage("Someone on this server already is using that name!");
						return true;
					}
				}
				
				worker.setName(args[0]);
				worker.sendMessage("Set nickname to: " + worker.getName());
			}
			else
			{
				worker.sendMessage("Not enough arguments!");
				return false;
			}
		}
		else
		{
				commander.sendMessage("Only clients have nicknames.");
		}
		
		return true;
	}
}
