package com.evanram.code.java.chatserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.evanram.code.java.chatserver.command.*;

public class Server
{
	private static int port = 38936;
	
	public static ServerSocket server;
	private static ArrayList<Socket> connectedClients = new ArrayList<Socket>();
	public static ArrayList<SessionWorker> clientWorkers = new ArrayList<SessionWorker>();
	
	private static ArrayList<String> operatorIps = new ArrayList<String>();	//FIXME: not workin' (at least for localhost)
	private static ArrayList<String> blacklistedIps = new ArrayList<String>();
	private static HashMap<String, RunCommand> registeredCommands = new HashMap<String, RunCommand>();
	
	private static String unformattedServerProperties = "";
	private static String messageOfTheDay = "";
	private static ArrayList<String> serverProperties = new ArrayList<String>();
	private static String superOperatorPassword;
	
	public Server()
	{
		try
		{
			//loads operators and server blacklist files
			loadOperators();
			loadBlacklist();
			
			unformattedServerProperties = //TODO: format this into serverProperties
					(String)loadTextFileToObject("server.properties", unformattedServerProperties);
			
			messageOfTheDay = 
					(String)loadTextFileToObject("motd.txt", messageOfTheDay);
			//TODO: write to log "server.log";
			
			//read from superop-password.txt and SHA-256 hash it to Server variable
			hashSuperOperatorPassword();
			
			for(String s : operatorIps)
			{
				System.out.println("OP: " + s);
			}
			for(String s : blacklistedIps)
			{
				System.out.println("BLACKLISTED: " + s);
			}
			System.out.println("PROPERTIES: " + unformattedServerProperties);
			System.out.println("SHA-256 hashed password: " + superOperatorPassword);
			
			//make sure we can use all the commands
			registerCommands();
		}
		catch (IOException e)
		{
			Message.logError("IOException in reading a server file.");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e)
		{
			Message.logError("NoSuchAlgorithmException in hashing super-operator password.");
			e.printStackTrace();
		}
		
		initializeConnections();
	}
	
	public static void main(String... args)
	{
		if(args.length > 0)	//try define port
		{
			try
			{
				port = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e)
			{
				Message.logError("Invalid port! Using default port: " + port);
			}
		}
		
		new Server();
	}
	
	public void registerCommands()
	{
		addCommand("help", new CommandHelp());
		addCommand("motd", new CommandMotd());
		addCommand("nick", new CommandNick());
		addCommand("list", new CommandList());
		addCommand("say", new CommandSay());
		addCommand("mute", new CommandMute());
		addCommand("msg", new CommandMsg());
		addCommand("stop", new CommandStop());
		//addCommand("kick", new CommandKick()); ***FIXME FIXME FIXME***
	}
	
	public static void initializeConnections()
	{
		try
		{
			server = new ServerSocket(port);
			Message.logInfo("Started server on port: " + server.getLocalPort());
		}
		catch(IOException e)
		{
			Message.logError("Encountered IOException on port connection.");
			System.exit(-1);
		}
		
		new Thread(new AcceptClientConnection()).start();	//handle incoming client connections
		
		//start accepting commands from the server
		new ServerCommand(Message.nextInputLine());
	}
	
	private static String getDataFolder()	//Referenced code from: http://stackoverflow.com/a/11166880
	{
		String parentPath = "";
		try
		{
			URL url = Server.class.getProtectionDomain().getCodeSource().getLocation();
			String pathToJar = URLDecoder.decode(url.getFile(), "UTF-8");
			parentPath = new File(pathToJar).getParentFile().getPath();
		}
		catch(Exception e)
		{
			Message.logError("Could not retrieve JAR path!");
		}
		
		return parentPath + File.separator + "chatserver/";
	}
	
	private static void loadOperators() throws IOException
	{
		loadTextFileToObject("operators.txt", operatorIps);
		
		for(String s : operatorIps)	//fix ips that aren't prefixed with '/', else no one is recognized
		{
			if(s.charAt(0) != '/')
				operatorIps.set(operatorIps.indexOf(s), "/" + s);
		}
	}
	
	private static void loadBlacklist() throws IOException
	{
		loadTextFileToObject("blacklist.txt", blacklistedIps);
		
		for(String s : blacklistedIps)	//see initOperators()
		{
			if(s.charAt(0) != '/')
				blacklistedIps.set(blacklistedIps.indexOf(s), "/" + s);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object loadTextFileToObject(String fileName, Object data) throws IOException
	{
		Message.logInfo("Loading " + fileName);
		File dir = new File(getDataFolder());
		dir.mkdirs();
		File file = new File(dir, fileName);
		file.createNewFile();
		
		BufferedReader read = new BufferedReader(new FileReader(file));
		String line;
		
		while((line = read.readLine()) != null)
		{
			if(data instanceof List)
				((List)data).add(line);
			else if(data instanceof String)
				data = ((String) data).concat(line + "\n");
		}
		
		if(data instanceof String)
		{	//remove \n character from end of String data.
			String dataStr = (String)data;
			if(dataStr.length() > 1)
			{
				if(dataStr.charAt(dataStr.length()-1) == '\n')
				{
					data = dataStr.substring(0,dataStr.length()-1);
				}
			}
		}
		
		read.close();
		return data;
	}

	private static void hashSuperOperatorPassword() throws IOException, NoSuchAlgorithmException
	{
		String unhashedPswd =
			(String)loadTextFileToObject("superop-password.txt", new String());
		
		MessageDigest pswdDigest = MessageDigest.getInstance("SHA-256");

		Server.superOperatorPassword = new String(pswdDigest.digest(unhashedPswd.getBytes("UTF-8")));
	}
	
	public static boolean matchesSuperOperatorPassword(String password)
	{
		try
		{
			MessageDigest pswdDigest = MessageDigest.getInstance("SHA-256");

			return (Server.superOperatorPassword.equals(
					new String(pswdDigest.digest(password.getBytes("UTF-8")))
					));
		}
		catch(Exception e)
		{
			Message.logError("Exception caught when comparing expression to superOperatorPassword.");
			return false;
		}
		
	}
	
	public static int getPort()
	{
		return port;
	}
	
	public static String getMotd()
	{
		return messageOfTheDay.equals("") ? 
				"Welcome to the Server! (Operators, change MOTD in /chatserver/motd.txt)" : messageOfTheDay;
	}
	
	public static ArrayList<Socket> getConnections()
	{
		return connectedClients;
	}
	
	public static ArrayList<SessionWorker> getClientWorkers()
	{
		return clientWorkers;
	}
	
	public static ArrayList<String> getOperatorIps()
	{
		return operatorIps;
	}
	
	public static ArrayList<String> getBlacklistedIps()
	{
		return blacklistedIps;
	}
	
	public static void addCommand(String command, RunCommand cmdObject)
	{
		registeredCommands.put(command, cmdObject);
	}
	
	public static HashMap<String, RunCommand> getRegisteredCommands()
	{
		return registeredCommands;
	}
	
	public static void stopServer()
	{
		try
		{
			Message.logInfo("Closed server.");
			System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	protected void finalize()
	{
		try
		{
			if(!server.isClosed())
			{
				Message.logInfo("Closed server socket.");
				server.close();
			}
			
			System.exit(0);
		}
		catch(IOException e)
		{
			Message.logError("Failed to close server socket!");
			System.exit(-1);
		}
	}
}
