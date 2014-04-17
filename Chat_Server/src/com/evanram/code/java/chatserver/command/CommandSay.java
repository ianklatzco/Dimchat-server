package com.evanram.code.java.chatserver.command;

import com.evanram.code.java.chatserver.Commander;
import com.evanram.code.java.chatserver.Message;
import com.evanram.code.java.chatserver.RunCommand;

public class CommandSay implements RunCommand
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
				String broadcast = "";
				
				for(String s : args)
				{
					broadcast = broadcast + " " + s;
				}
				
				Message.broadcast(broadcast);
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
