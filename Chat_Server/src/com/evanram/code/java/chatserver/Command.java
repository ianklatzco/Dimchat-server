package com.evanram.code.java.chatserver;

import java.util.Arrays;
import java.util.HashMap;

public class Command
{
	private Commander commander;
	private String command;
	private String[] args;
	private HashMap<String, RunCommand> registeredCommands = Server.getRegisteredCommands();
	
	/**
	 * Command constructor. The commander is whoever sent the command.
	 * Processes command.
	 * @param commander
	 * @param command
	 */
	public Command(Commander commander, String command)
	{
		this.commander = commander;
		this.command = command;
		
		processCommand();
	}
	
	/**
	 * Call formatCommandString() to clean up the command String.
	 * If the Server's registered commands contains the command, call its execute(Commander, String[]) method.
	 */
	private void processCommand()
	{
		formatCommandString();
		
		if(!registeredCommands.containsKey(command))
		{
			commander.sendMessage("Bad command! Type \'!help\' for help.");
			return;
		}
		
		//TODO: if 'execute' -> else 'commander.sendMessage(registeredCommands.get(command).usage());'
		registeredCommands.get(command).execute(commander, args);
	}
	
	/**
	 * Clean up command String and processArguments().
	 */
	private void formatCommandString()
	{
		if(command.length() > 1)
		{
			if(command.charAt(0) == '!')
				command = command.substring(1);
		}
		
		processArguments();
		
		command = command.toLowerCase();
	}

	/**
	 * Generates an argument String array (each split by a space).
	 */
	private void processArguments()
	{
		command = command.replace("\\s+", "");
		String[] argsWithCmd = command.split(" ");	//arguments that contain command
		command = argsWithCmd[0];
		
		args = Arrays.copyOfRange(argsWithCmd, 1, argsWithCmd.length);	//remove command from args
	}
}

