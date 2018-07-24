package com.rsisland.plugin.teleportplugin.data;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.rsisland.plugin.teleportplugin.TeleportPlugin;

public class TPPlayer
{
	//TODO: PerPlayer policy
	private final TeleportPlugin plugin;
	private final UUID uuid;
	
	private Policy tpReceivePolicy;
	private RequestManager requestManager = new RequestManager();
	
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
		return tpReceivePolicy;
	}
	
	public void playerLeft(Player player)
	{
		requestManager.playerLeft(player);
	}
	
	public RequestManager getRequestManager()
	{
		return requestManager;
	}
}
