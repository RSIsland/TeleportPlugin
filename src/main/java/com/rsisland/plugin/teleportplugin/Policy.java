package com.rsisland.plugin.teleportplugin;

import java.util.HashMap;
import java.util.Map;

public enum Policy
{
	ACCEPT(1),
	DENY(2),
	REQUEST(3),
	;
	
	private final int dbID;
	
	private Policy(int dbID)
	{
		this.dbID = dbID;
	}
	
	public int getIdDB()
	{
		return dbID;
	}
	
	//#########################################################################
	
	private static final Map<Integer, Policy> dbIDmapping = new HashMap<>();
	
	static
	{
		for(Policy policy : values())
		{
			dbIDmapping.put(policy.getIdDB(), policy);
		}
	}
	
	public static Policy getPolicyFromID(int id)
	{
		return dbIDmapping.get(id);
	}
	
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
