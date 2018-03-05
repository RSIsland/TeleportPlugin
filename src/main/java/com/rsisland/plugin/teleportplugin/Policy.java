package com.rsisland.plugin.teleportplugin;

public enum Policy
{
	ACCEPT,
	REQUEST,
	DENY,
	;
	
	public static Policy fromString(String policy)
	{
		switch(policy)
		{
		case "accept":
			return Policy.ACCEPT;
		case "request":
			return Policy.REQUEST;
		case "deny":
			return Policy.DENY;
		default:
			return null;
		}
	}
}
