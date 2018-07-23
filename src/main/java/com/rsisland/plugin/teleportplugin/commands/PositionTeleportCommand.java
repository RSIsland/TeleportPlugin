package com.rsisland.plugin.teleportplugin.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.ecconia.rsisland.framework.cofami.Subcommand;
import com.ecconia.rsisland.framework.cofami.exceptions.NoPermissionException;
import com.ecconia.rsisland.framework.commonelements.Cuboid;
import com.rsisland.plugin.teleportplugin.TeleportPlugin;

public class PositionTeleportCommand extends Subcommand
{
	private final TeleportPlugin plugin;
	
	private String PERM_TP_POS = "tpp.pos";
	private String PERM_TP_OTHER_POS = "tpp.otherpos";
	
	public PositionTeleportCommand(TeleportPlugin plugin)
	{
		super("tpp");
		this.plugin = plugin;
	}
	
	@Override
	public void exec(CommandSender sender, String[] arguments)
	{
		int count = arguments.length;
		
		if(count == 3)
		{
			if(!sender.hasPermission(PERM_TP_POS))
			{
				//TODO: Check if first arg is a playername. 
				throw new NoPermissionException(path + " <x> <y> <z>");
			}
			
			Player player = getPlayer(sender);
			Location target = parseLocation(player.getLocation(), arguments[0], arguments[1], arguments[2]);
			
			if(player.teleport(target, TeleportCause.COMMAND))
			{
				f.n(player, "Teleported to %v, %v, %v.", target.getBlockX(), target.getBlockY(), target.getBlockZ());
			}
			else
			{
				f.e(player, "Teleport was not possible.");
			}
		}
		else if(count == 4)
		{
			if(!sender.hasPermission(PERM_TP_OTHER_POS))
			{
				throw new NoPermissionException(path + " <player> <x> <y> <z>");
			}
			
			Player player = plugin.getUtils().getPlayer(sender, arguments[0]);
			
			if(player == null)
			{
				die("Player %v does not seem to be online.", arguments[0]);
			}
			
			Location target = parseLocation(player.getLocation(), arguments[1], arguments[2], arguments[3]);
			
			if(player.teleport(target, TeleportCause.COMMAND))
			{
				if(sender != player)
				{
					f.n(sender, "Teleported %v to %v, %v, %v.", player.getName(), target.getBlockX(), target.getBlockY(), target.getBlockZ());
					//TODO: By?
				}
				
				f.n(player, "Teleported to %v, %v, %v.", target.getBlockX(), target.getBlockY(), target.getBlockZ());
			}
			else
			{
				f.e(sender, "Teleport was not possible.");
			}
		}
		else
		{
			List<String> usages = new ArrayList<>();
			
			if((sender instanceof Player) && sender.hasPermission(PERM_TP_POS))
			{
				usages.add(path + " <x> <y> <z>");
			}
			
			if(sender.hasPermission(PERM_TP_OTHER_POS))
			{
				usages.add(path + " <player> <x> <y> <z>");
			}
			
			die("Usage: " + StringUtils.repeat("%v", " and ", usages.size()), usages.toArray(new Object[0]));
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] arguments)
	{
		if(arguments.length == 1 && sender.hasPermission(PERM_TP_OTHER_POS))
		{
			return plugin.getUtils().getPlayerNames(sender, arguments[0], sender.getName());
		}
		
		return Collections.emptyList();
	}
	
	// LOCATION PARSING #######################################################
	
	private Location parseLocation(Location originLocation, String sx, String sy, String sz)
	{
		boolean sx_rel = sx.charAt(0) == '~';
		boolean sy_rel = sy.charAt(0) == '~';
		boolean sz_rel = sz.charAt(0) == '~';
		
		double dx = 0;
		double dy = 0;
		double dz = 0;
		
		if(sx_rel)
		{
			sx = sx.substring(1);
		}
		
		if(sy_rel)
		{
			sy = sy.substring(1);
		}
		
		if(sz_rel)
		{
			sz = sz.substring(1);
		}
		
		List<String> cannotParse = new ArrayList<>();
		
		try
		{
			dx = sx.isEmpty() ? 0 : parseD(sx);
			if(sx_rel)
			{
				dx += originLocation.getX();
			}
		}
		catch(NumberFormatException e)
		{
			cannotParse.add(sx);
		}
		
		try
		{
			dy = sy.isEmpty() ? 0 : parseD(sy);
			if(sy_rel)
			{
				dy += originLocation.getY();
			}
		}
		catch(NumberFormatException e)
		{
			cannotParse.add(sy);
		}
		
		try
		{
			dz = sz.isEmpty() ? 0 : parseD(sz);
			if(sz_rel)
			{
				dz += originLocation.getZ();
			}
		}
		catch(NumberFormatException e)
		{
			cannotParse.add(sz);
		}
		
		if(!cannotParse.isEmpty())
		{
			//TODO: Message ok?
			die("Cannot parse: " + StringUtils.repeat("%v", " and ", cannotParse.size()), cannotParse.toArray(new Object[0]));
		}
		
		Location target = new Location(originLocation.getWorld(), dx, dy, dz);
		
		Cuboid bounds = plugin.getWorldBoundaries(target.getWorld());
		
		if(bounds != null && bounds.getFirstPoint().getX() >= target.getBlockX() && bounds.getSecondPoint().getX() <= target.getBlockX()
						  && bounds.getFirstPoint().getY() >= target.getBlockY() && bounds.getSecondPoint().getY() <= target.getBlockY()
						  && bounds.getFirstPoint().getZ() >= target.getBlockZ() && bounds.getSecondPoint().getZ() <= target.getBlockZ())
		{
			die("Teleport position outside of allowed world boundaries. %v: %v to %v %v: %v to %v %v: %v to %v.", 
					"x", bounds.getFirstPoint().getX(), bounds.getSecondPoint().getX(), 
					"y", bounds.getFirstPoint().getY(), bounds.getSecondPoint().getY(), 
					"z", bounds.getFirstPoint().getZ(), bounds.getSecondPoint().getZ());
		}
		
		return target;
	}
	
	private static Double parseD(String value)
	{
		double d = Double.parseDouble(value.replaceAll(",", "\\."));
		if(!Double.isFinite(d))
		{
			throw new NumberFormatException();
		}
		return d;
	}
	
	// PERMISSIONS ############################################################
	
	//Overwritten to check multiple permissions instead of one
	@Override
	protected boolean hasPermission(CommandSender sender)
	{
		return sender.hasPermission(PERM_TP_POS) || sender.hasPermission(PERM_TP_OTHER_POS);
	}
	
	//Overwritten to set the permissions for the Bukkit call command permissions
	@Override
	public String getPermissions()
	{
		return PERM_TP_POS + ";" + PERM_TP_OTHER_POS;
	}
}
