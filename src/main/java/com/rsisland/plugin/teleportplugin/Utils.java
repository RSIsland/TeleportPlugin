package com.rsisland.plugin.teleportplugin;

import java.util.Collection;

import org.bukkit.entity.Player;

public class Utils
{
	public static Player getPlayer(Collection<? extends Player> playerCollection, String input)
	{
		//TODO: Add gobal filter
		for(Player player : playerCollection)
		{
			if(player.getName().equalsIgnoreCase(input))
			{
				return player;
			}
		}
		
		return null;
	}
}
