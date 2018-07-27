package com.rsisland.plugin.teleportplugin;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rsisland.plugin.teleportplugin.api.PlayerFilter;

import de.ecconia.bukkit.plugin.jsonsender.JSONPluginAPI;

public class Utils
{
	private final TeleportPlugin plugin;
	private final JSONPluginAPI json;
	
	public Utils(TeleportPlugin plugin)
	{
		this.plugin = plugin;
		
		//Setup JSON code
		if(plugin.getServer().getPluginManager().isPluginEnabled("JSONPlugin"))
		{
			json = (JSONPluginAPI) plugin.getServer().getPluginManager().getPlugin("JSONPlugin");
		}
		else
		{
			json = null;
		}
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
			//Check if there is exactly this player in the list
			for(Player p : playersStartingWithInput)
			{
				if(p.getName().toLowerCase().equals(input))
				{
					return p;
				}
			}
			
			//Else take just any...
			return playersStartingWithInput.get(0);
		}
	}
	
	public List<Player> getPlayers(CommandSender completer, String input)
	{
		String lowercase = input.toLowerCase();
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
					 .filter(player -> player.getName().toLowerCase().startsWith(lowercase))
					 .collect(Collectors.toList());
	}
	
	public List<String> getPlayerNames(CommandSender completer, String input, String filterName)
	{
		String lowercase = input.toLowerCase();
		return plugin.getServer().getOnlinePlayers().stream()
				 .filter(player -> !player.getName().equals(filterName))
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
				 .filter(player -> player.getName().toLowerCase().startsWith(lowercase))
				 .map(Player::getName)
				 .collect(Collectors.toList());
	}
	
	public boolean json(Player player, String jsonMessage)
	{
		if(json != null)
		{
			return json.json(player, jsonMessage);
		}
		else
		{
			return false;
		}
	}
}
