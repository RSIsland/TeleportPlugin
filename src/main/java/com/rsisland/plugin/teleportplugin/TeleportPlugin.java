package com.rsisland.plugin.teleportplugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.ecconia.rsisland.framework.cofami.CommandHandler;
import com.ecconia.rsisland.framework.cofami.Feedback;
import com.rsisland.plugin.teleportplugin.commands.PolicyCommand;
import com.rsisland.plugin.teleportplugin.commands.TeleportCommand;

public class TeleportPlugin extends JavaPlugin implements Listener
{
	private Map<Player, TPPlayer> tpPlayers;
	
	@Override
	public void onEnable()
	{
		tpPlayers = new HashMap<>();
		
		Feedback f = new Feedback(Feedback.simplePrefix(ChatColor.WHITE, ChatColor.GOLD, "TP"));
		
		new CommandHandler(this, f
			,new TeleportCommand(this
				,new PolicyCommand(this)
			)
		);
		
		/*
		
		/tp <player>
		
		/tp accept
		/tp deny
		
		/tp policy 
		 		accept - everyone can just tp
		 		request (default) - everyone has to send a request
		 		deny - nobody can tp
		
		 */
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		tpPlayers.remove(event.getPlayer());
		//Dump requests
	}
	
	public TPPlayer getTPPlayer(Player player)
	{
		TPPlayer tpPlayer = tpPlayers.get(player);
		if(tpPlayer == null)
		{
			tpPlayer = new TPPlayer();
			tpPlayers.put(player, tpPlayer);
		}
		return tpPlayer;
	}
}
