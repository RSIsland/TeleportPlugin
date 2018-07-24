package com.rsisland.plugin.teleportplugin.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

public class RequestManager
{
	Map<Player, Long> fromTPRequests = new HashMap<>();
	
	public void createRequest(Player player)
	{
		fromTPRequests.put(player, System.currentTimeMillis());
	}
	
	public boolean accept(Player player)
	{
		Long requestTime = fromTPRequests.remove(player);
		if(requestTime == null)
		{
			//Dude did not request to tp to this player.
			return false;
		}
		
		if(timePastInSeconds(requestTime) > 120)
		{
			//Dude did request the tp over two mins ago.
			return false;
		}
		
		//There is a pending request and it is valid.
		return true;
	}
	
	private static int timePastInSeconds(Long pastStamp)
	{
		return (int) ((System.currentTimeMillis() - pastStamp) / 1000);
	}
	
	public void playerLeft(Player player)
	{
		fromTPRequests.remove(player);
	}

	
	public List<Player> getAll()
	{
		List<Player> pending = new ArrayList<>();
		
		for(Entry<Player, Long> request : fromTPRequests.entrySet())
		{
			if(timePastInSeconds(request.getValue()) <= 120)
			{
				pending.add(request.getKey());
			}
		}
		
		fromTPRequests.clear();
		return pending;
	}
}
