package com.evanram.code.java.chatserver;

/**
 * Interface that all commands must implement.
 */
public interface RunCommand
{
	String[] aliases();
	String description();
	String usage();
	boolean execute(Commander commander, String[] args);
}
