package com.rsisland.plugin.teleportplugin;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rsisland.plugin.teleportplugin.api.PlayerFilter;

public class Utils
{
	private final TeleportPlugin plugin;
	
	public Utils(TeleportPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public Player getPlayer(CommandSender completer, String input)
	{
		//TODO: Levenstein? Well not yet.
		List<Player> playersStartingWithInput = getPlayers(completer, input);
		
		if(playersStartingWithInput.isEmpty())
		{
			return null;
		}
		else
		{
			return playersStartingWithInput.get(0);
		}
	}
	
	public List<Player> getPlayers(CommandSender completer, String input)
	{
		return plugin.getServer().getOnlinePlayers().stream()
					 .filter(player -> {
						 for(PlayerFilter filter : plugin.getPlayerFilters())
						 {
							 if(filter.test(completer, player))
							 {
								 return false;
							 }
						 }
						 return true;
					 })
					 .filter(player -> player.getName().equalsIgnoreCase(input))
					 .collect(Collectors.toList());
	}
}
