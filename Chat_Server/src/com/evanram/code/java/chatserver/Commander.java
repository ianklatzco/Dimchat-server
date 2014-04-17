package com.evanram.code.java.chatserver;

public interface Commander
{
	boolean isOp();
	boolean isBlacklisted();
	String getName();
	void setName(String newName);
	void sendMessage(String message);
}
