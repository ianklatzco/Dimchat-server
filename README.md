Dimchat (Server)
==============

Server application to host Dimchat clients. 

Dimchat-server is a small chat-server application for Dimchat-supporting clients.
Clients connect to the server and can chat with one another, or utilize an extenable array of commands.
Constantly being improved and tweaked.

What's the "dim" for?

Dim refers to a variety of meanings concerning this application. One, for instance, is that stand-alone chat applications are becoming less common due to them being implemented into more grand services or applications (such as chatting in video games). Here, dim would stand for the dim mood of a chat without boundaries. Talk about whatever you want, don't be constricted by lights that point you towards inside-the-box thinking. Just be able to converse naturally.

How would I go about adding my own commands to the server?

Good question! Luckily, commands are pretty simple to set up and register to the server. You'll want to start by creating a class for your command. Let's create a command to get a client's IP address. All commands must implement the interface RunCommand. If you're using an IDE, you should be able to quickly add all abstract methods from it. The execute(Commander, String[] args) method is called when the command is run. The Commander (person sending command) will be sent the usage() String if execute(Commander, String[]) returns false. To register the command, you must modify the method registerCommands() in class Server by adding the new command.

First, create the command. Make sure it implements the RunCommand interface.

```java
public class CommandGetIP implements RunCommand
{

	@Override
	public String[] aliases() //Aliases will point to this command. Unimplemented.
	{
		return new String[]{"ipget", "getclientinfo"};
	}

	@Override
	public String description() //For command information. Unimplemented.
	{
		return "Retrieve the IP address of a client.";
	}

	@Override
	public String usage() //Called if execute(Commander, String[]) returns false. Unimplemented.
	{
		return "!COMMAND <client-name>";
	}

	@Override
	public boolean execute(Commander commander, String[] args) //Called when command is run.
	{
		if(args.length > 0)
		{
			for(SessionWorker worker : Server.getSessionWorkers())
			{
				if(worker.getName().equalsIgnoreCase(args[0])
				{
					commander.sendMessage("IP of " + args[0] + " is: " + worker.getIp());
					return true;
				}
			}

			commander.sendMessage("No client with that name is online.");
		}
		else
		{
			commander.sendMessage("Not enough arguments!")
			return false;
		}

		return true;
	}

}
```

Now, register the command.

```java
	//Server class
	public void registerCommands()
	{
		//Other commands up here...
		addCommand("getip", new CommandGetIP()); //Your newly registered command!
	}
```

Done, you've created your own custom command for Dimchat.

Copyright 2014 Evan Ram.
