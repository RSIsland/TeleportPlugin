package com.rsisland.plugin.teleportplugin;

import java.sql.SQLException;
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
import com.rsisland.plugin.teleportplugin.db.DBAdapter;

public class TeleportPlugin extends JavaPlugin implements Listener
{
	private Map<Player, TPPlayer> tpPlayers;
	private DBAdapter dba;
	private FormattedLogger logger;
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		
		tpPlayers = new HashMap<>();
		
		Feedback f = new Feedback(Feedback.simplePrefix(ChatColor.WHITE, ChatColor.GOLD, "TP"));
		logger = new FormattedLogger(f, getServer().getConsoleSender());
		
		String url = getConfig().getString("database.url");
		try
		{
			dba = new DBAdapter(url, logger);
		}
		catch (SQLException e)
		{
			logger.error("Could not init the database connection. DB not supported.");
			e.printStackTrace();
		}
		
		getServer().getPluginManager().registerEvents(this, this);
		
		new CommandHandler(this, f
			,new TeleportCommand(this
				,new PolicyCommand(this)
			)
		);
	}
	
	public DBAdapter getDBA()
	{
		return dba;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		tpPlayers.remove(event.getPlayer());
	}
	
	public TPPlayer getTPPlayer(Player player)
	{
		TPPlayer tpPlayer = tpPlayers.get(player);
		if(tpPlayer == null)
		{
			tpPlayer = new TPPlayer(this, player.getUniqueId());
			tpPlayers.put(player, tpPlayer);
		}
		return tpPlayer;
	}
}
