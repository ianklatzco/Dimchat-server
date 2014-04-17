package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.RunCommand;
import com.evanram.code.java.chatserver.Server;
import com.evanram.code.java.chatserver.ServerCommand;

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
	public boolean execute(Commander commander, String[] args)	//TODO: operators with password can shut it down
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
				Server.stopServer();
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
