package com.rsisland.plugin.teleportplugin;

import java.util.UUID;

public class TPPlayer
{
	private Policy tpReceivePolicy;
	
	private final TeleportPlugin plugin;
	private final UUID uuid;
	
	public TPPlayer(TeleportPlugin plugin, UUID uuid)
	{
		this.plugin = plugin;
		this.uuid = uuid;
		
		if(plugin.getDBA() != null)
		{
			tpReceivePolicy = plugin.getDBA().loadPolicy(uuid);
		}
	}

	public void setTpReceivePolicy(Policy tpReceivePolicy)
	{
		this.tpReceivePolicy = tpReceivePolicy;
		if(plugin.getDBA() != null)
		{
			plugin.getDBA().savePolicy(uuid, tpReceivePolicy);
		}
	}
	
	public Policy getTpReceivePolicy()
	{
		if(tpReceivePolicy == null)
		{
			//TODO: Impl Request
			return Policy.ACCEPT;
		}
		return tpReceivePolicy;
	}
}
