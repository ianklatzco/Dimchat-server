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
	public static ArrayList<SessionWorker> sessionWorkers = new ArrayList<SessionWorker>();
	
	private static ArrayList<String> operatorIps = new ArrayList<String>();	//FIXME: not workin' (at least for localhost)
	private static ArrayList<String> blacklistedIps = new ArrayList<String>();
	private static HashMap<String, RunCommand> registeredCommands = new HashMap<String, RunCommand>();
	
	private static String unformattedServerProperties = "";
	private static String messageOfTheDay = "";
	private static ArrayList<String> serverProperties = new ArrayList<String>();
	private static String superOperatorPassword;
	
	private static ServerCommand serverCommandInstance;
	
	/**
	 * Server constructor.
	 * Loads all files in /chatserver/~ (relative to jar file's path).
	 * Register commands to their RunCommand Object.
	 * Start socket connection.
	 */
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
	
	/**
	 * Parse command line arguments, then call Server() constructor.
	 * @param args
	 */
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
	
	/**
	 * Every command must be 'registered' to a RunCommand Object.
	 * addCommand(String, RunCommand) is to be used multiple times here.
	 */
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
	
	/**
	 * Create server socket, then start accepting client connections in a new Thread.
	 * Finally, accept server commands from console.
	 */
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
		serverCommandInstance = new ServerCommand(Message.nextInputLine());
	}
	
	/**
	 * Get the path to this program's jar file.
	 * Referenced from: http://stackoverflow.com/a/11166880
	 * @return
	 */
	private static String getDataFolder()
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
	
	/**
	 * For each IP in operators.txt, add it to the operatorIps ArrayList.
	 * If the first character of each entry is not '/', add it.
	 * (IP must start with '/' or else server does not recognize the String as an IP)
	 * @throws IOException
	 */
	private static void loadOperators() throws IOException
	{
		loadTextFileToObject("operators.txt", operatorIps);
		
		for(String s : operatorIps)	//fix ips that aren't prefixed with '/', else no one is recognized
		{
			if(s.charAt(0) != '/')
				operatorIps.set(operatorIps.indexOf(s), "/" + s);
		}
	}
	
	/**
	 * For each IP in blacklist.txt, add it to the blacklistedIps ArrayList.
	 * If the first character of each entry is not '/', add it.
	 * (IP must start with '/' or else server does not recognize the String as an IP)
	 * @throws IOException
	 */
	private static void loadBlacklist() throws IOException
	{
		loadTextFileToObject("blacklist.txt", blacklistedIps);
		
		for(String s : blacklistedIps)	//see initOperators()
		{
			if(s.charAt(0) != '/')
				blacklistedIps.set(blacklistedIps.indexOf(s), "/" + s);
		}
	}
	
	/**
	 * Read from file in /chatserver/fileName to Object.
	 * If Object instanceof String, separate it with newline characters.
	 * If Object is an instanceof a List, add each line to a new Object in the List.
	 * @param fileName
	 * @param data
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * Reads unhashed password in superop-password.txt and SHA-256 hashes it to superOperatorPassword.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private static void hashSuperOperatorPassword() throws IOException, NoSuchAlgorithmException
	{
		String unhashedPswd =
			(String)loadTextFileToObject("superop-password.txt", new String());
		
		MessageDigest pswdDigest = MessageDigest.getInstance("SHA-256");

		Server.superOperatorPassword = new String(pswdDigest.digest(unhashedPswd.getBytes("UTF-8")));
	}
	
	/**
	 * SHA-256 hash parameter. Return true if parameter matches superOperatorPassword.
	 * @param password
	 * @return
	 */
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
	
	/**
	 * Get the port this server is running on.
	 * @return
	 */
	public static int getPort()
	{
		return port;
	}
	
	/**
	 * If motd.txt is not empty, return the contents of that. 
	 * Else, return default message of the day.
	 * @return
	 */
	public static String getMotd()
	{
		return messageOfTheDay.equals("") ? 
				"Welcome to the Server! (Operators, change MOTD in /chatserver/motd.txt)" : messageOfTheDay;
	}
	
	/**
	 * Get all client Socket connections to the server.
	 * @return
	 */
	public static ArrayList<Socket> getConnections()
	{
		return connectedClients;
	}
	
	/**
	 * Get all connected SessionWorkers (clients).
	 * @return
	 */
	public static ArrayList<SessionWorker> getSessionWorkers()
	{
		return sessionWorkers;
	}
	
	/**
	 * Get the ArrayList of IPs who are operators.
	 * @return
	 */
	public static ArrayList<String> getOperatorIps()
	{
		return operatorIps;
	}
	
	/**
	 * Get the ArrayList of IPs who are banned.
	 * @return
	 */
	public static ArrayList<String> getBlacklistedIps()
	{
		return blacklistedIps;
	}
	
	/**
	 * Gets instance of ServerCommand (the console).
	 * @return
	 */
	public static ServerCommand getServerCommandInstance()
	{
		return serverCommandInstance;
	}
	
	/**
	 * Registers a command.
	 * Add a command to the registeredCommands HashMap.
	 * @param command
	 * @param cmdObject
	 */
	public static void addCommand(String command, RunCommand cmdObject)
	{
		registeredCommands.put(command, cmdObject);
	}
	
	/**
	 * Get String, RunCommand HashMap of commands that have been registered.
	 * @return
	 */
	public static HashMap<String, RunCommand> getRegisteredCommands()
	{
		return registeredCommands;
	}
	
	/**
	 * Stops the server.
	 */
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
	
	/**
	 * Try to safely close server socket.
	 */
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
