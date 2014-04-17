package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;
import com.evanram.code.java.chatserver.ServerCommand;

/**
 * Send stop signal to server. Requires super operator password if Commander is a SessionWorker.
 */
public class CommandStop implements RunCommand
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
			boolean unlocked = false;
			
			if(commander instanceof ServerCommand)
			{
				unlocked = true;
			}
			else if(args.length > 0 && Server.matchesSuperOperatorPassword(args[0]))
			{
				unlocked = true;
			}
			
			if(unlocked)
			{
				commander.sendMessage("Sending shutdown signal...");
				Server.stopServer();
			}
			else
				commander.sendMessage("Wrong password. Use super operator password as first argument to stop server.");
		}
		else
		{
			commander.sendMessage("Only an operator may issue the stop command.");
		}
		return true;
	}

}
