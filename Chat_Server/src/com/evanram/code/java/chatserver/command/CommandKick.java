package com.evanram.code.java.chatserver.command;

import java.io.IOException;

import com.evanram.code.java.chatserver.SessionWorker;
import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.Message;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;

public class CommandKick implements RunCommand
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
		//FIXME!!! essentially breaks server
		if(commander.isOp())
		{
			if(args.length > 0)
			{
				String name = args[0];
				String ip = args[0].charAt(0) == '/' ? args[0] : "/" + args[0];
				
				for(SessionWorker w : Server.getSessionWorkers())
				{
					if(w.getName().equalsIgnoreCase(name) || w.getClient().getInetAddress().toString().equals(ip))
					{
						try
						{
							w.getClient().close();
						}
						catch (IOException e)
						{
							commander.sendMessage("Could not kick that client!");
							Message.logError("Exception when kicking client: " + w.toString());
							e.printStackTrace();
						}
					}
				}
				
			}
			else
			{
				commander.sendMessage("Not enough arguments!");
			}
		}
		else
		{
			commander.sendMessage("You must be an operator to kick clients.");
		}
		return true;
	}
	
}
