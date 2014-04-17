package com.evanram.code.java.chatserver;

import java.io.IOException;
import java.net.Socket;

public class AcceptClientConnection implements Runnable
{
	/**
	 * Create a new SessionWorker each time the server receives a new client connection.
	 */
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Socket nextClientConnection = Server.server.accept();
				
				//TODO: remove ip upon disconnect
				Server.getConnections().add(nextClientConnection);
				
				
				//create new client thread
				final SessionWorker worker = new SessionWorker(nextClientConnection);
				
				//handle banned users
				if(worker.isBlacklisted())
				{
					String workerIp = worker.getClient().getInetAddress().toString();
					
					worker.getClient().close();	//kicks user
					Message.logInfo("Disconnected banned user (" + workerIp + ")");
					continue;
				}
				
				Server.sessionWorkers.add(worker);
				new Thread(worker).start();
				
				Message.logInfo("Client connection from: " + 
						nextClientConnection.getInetAddress().toString());
				
				new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									Thread.sleep(1000);
								}
								catch(InterruptedException e)
								{
									Message.logError("InterruptedException in waiting before broadcasting client join message.");
								}
								Message.broadcast(worker.getName() + " has connected.");
							}
						}).start();
			}
			catch(IOException e)
			{
				Message.logError("Encountered IOException on client connection.");
			}
		}
	}
}
