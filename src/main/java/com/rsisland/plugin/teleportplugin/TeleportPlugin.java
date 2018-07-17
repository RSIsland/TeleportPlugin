package com.rsisland.plugin.teleportplugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.ecconia.rsisland.framework.cofami.CommandHandler;
import com.ecconia.rsisland.framework.cofami.Feedback;
import com.ecconia.rsisland.framework.commonelements.Cuboid;
import com.rsisland.plugin.teleportplugin.api.PlayerFilter;
import com.rsisland.plugin.teleportplugin.commands.PolicyCommand;
import com.rsisland.plugin.teleportplugin.commands.TeleportCommand;
import com.rsisland.plugin.teleportplugin.db.DBAdapter;

public class TeleportPlugin extends JavaPlugin implements Listener
{
	private Map<Player, TPPlayer> tpPlayers;
	private DBAdapter dba;
	private FormattedLogger logger;
	
	//API:
	private List<PlayerFilter> playerFilters = new ArrayList<>();
	private Map<World, Cuboid> worldBoundaries = new HashMap<>();
	
	//Other:
	private Utils utils;
	
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
		
		utils = new Utils(this);
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
	
	public Utils getUtils()
	{
		return utils;
	}
	
	// API Data Provider methods ##############################################
	
	public void setWorldBounds(World world, Cuboid bounds)
	{
		worldBoundaries.put(world, bounds);
	}
	
	public void addPlayerFilter(PlayerFilter playerFilter)
	{
		playerFilters.add(playerFilter);
	}
	
	public Cuboid getWorldBoundaries(World world)
	{
		return worldBoundaries.get(world);
	}

	public List<PlayerFilter> getPlayerFilters()
	{
		return playerFilters;
	}
}
