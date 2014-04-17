package com.evanram.code.java.chatserver;

public interface RunCommand
{
	String[] aliases();
	String description();
	String usage();
	boolean execute(Commander commander, String[] args);
}
