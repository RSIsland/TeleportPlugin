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

public class TeleportPlugin extends JavaPlugin implements Listener
{
	private Map<Player, TPPlayer> tpPlayers;
	private DBAdapter dba;
	private Feedback f;
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		
		tpPlayers = new HashMap<>();

		f = new Feedback(Feedback.simplePrefix(ChatColor.WHITE, ChatColor.GOLD, "TP"));
		
		try
		{
			String url = getConfig().getString("database.url");
			dba = new DBAdapter(url, new FormattedLogger(f, getServer().getConsoleSender()));
			try
			{
				dba.createTables();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				f.e(getServer().getConsoleSender(), "Error creating required tables, disabling DB support.");
				try
				{
					dba.close();
				}
				catch(SQLException e1)
				{
					f.e(getServer().getConsoleSender(), "Error while closing DB connection.");
					e1.printStackTrace();
				}
				dba = null;
			}
		}
		catch(ClassNotFoundException e)
		{
			f.e(getServer().getConsoleSender(), "Could not find driver for database - nag Plugin author - he did not add it, disabling DB support.");
		}
		catch(SQLException e)
		{
			f.e(getServer().getConsoleSender(), "Error connecting to DB, see stacktrace, disabling DB support.");
			e.printStackTrace();
		}
		
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
		//Dump requests
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
