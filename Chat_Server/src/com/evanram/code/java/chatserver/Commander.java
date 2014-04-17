package com.evanram.code.java.chatserver;

/**
 * A commander is anyone who can send commands.
 * Both CommandServer and SessionWorker are Commanders because they can send commands to the server.
 */
public interface Commander
{
	boolean isOp();
	boolean isBlacklisted();
	String getName();
	void setName(String newName);
	void sendMessage(String message);
}
