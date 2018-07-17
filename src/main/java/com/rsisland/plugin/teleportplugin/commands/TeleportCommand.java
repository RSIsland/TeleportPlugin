package com.rsisland.plugin.teleportplugin.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.ecconia.rsisland.framework.cofami.Feedback;
import com.ecconia.rsisland.framework.cofami.GroupSubcommand;
import com.ecconia.rsisland.framework.cofami.Subcommand;
import com.ecconia.rsisland.framework.cofami.exceptions.NoPermissionException;
import com.ecconia.rsisland.framework.commonelements.Cuboid;
import com.rsisland.plugin.teleportplugin.Policy;
import com.rsisland.plugin.teleportplugin.TeleportPlugin;

public class TeleportCommand extends GroupSubcommand
{
	private final TeleportPlugin plugin;
	
	private String[] permissions;
	private String PERM_TP = "tp";
	private String PERM_TP_POS = "tppos";
	private String PERM_TP_OTHER = "othertp";
	private String PERM_TP_OTHER_POS = "othertppos";
	
	public TeleportCommand(TeleportPlugin plugin, Subcommand... subcommands)
	{
		super("tp", subcommands);
		this.plugin = plugin;
	}
	
	@Override
	protected void exec(CommandSender sender) //No arguments
	{
		List<String> usages = new ArrayList<>();
		
		if(sender instanceof Player)
		{
			if(sender.hasPermission(PERM_TP))
			{
				usages.add(path + " <player>");
			}
			
			if(sender.hasPermission(PERM_TP_POS))
			{
				usages.add(path + " <x> <y> <z>");
			}
		}
		
		if(sender.hasPermission(PERM_TP_OTHER))
		{
			usages.add(path + " <player> <player>");
		}
		
		if(sender.hasPermission(PERM_TP_OTHER_POS))
		{
			usages.add(path + " <player> <x> <y> <z>");
		}
		
		usages.add(path + " policy ...");
		
		die("Usage: " + StringUtils.repeat("%v", " and ", usages.size()), usages.toArray(new Object[0]));
	}
	
	@Override
	protected void noMatch(CommandSender sender, String[] arguments) //Arguments are not a subcommand
	{
		int count = arguments.length;
		
		if(count == 1)
		{
			if(!sender.hasPermission(PERM_TP))
			{
				throw new NoPermissionException(path + " <player>");
			}
			
			//TODO: Proper feedback?
			Player player = getPlayer(sender);
			String playerName = arguments[0];
			
			Player target = plugin.getUtils().getPlayer(player, playerName);
			
			tp(player, target, playerName);
		}
		else if(count == 3)
		{
			if(!sender.hasPermission(PERM_TP_POS))
			{
				throw new NoPermissionException(path + " <x> <y> <z>");
			}
			
			//TODO: Proper feedback?
			Player player = getPlayer(sender);
			Location target = parseLocation(player.getLocation(), arguments[0], arguments[1], arguments[2]);
			
			if(player.teleport(target, TeleportCause.COMMAND))
			{
				f.n(player, "Successfully teleported.");
			}
			else
			{
				f.e(player, "Teleport was not possible.");
			}
		}
		else if(count == 2)
		{
			Player player = plugin.getUtils().getPlayer(sender, arguments[0]);
			Player target = plugin.getUtils().getPlayer(sender, arguments[1]);
			
			if(player == sender)
			{
				if(!sender.hasPermission(PERM_TP))
				{
					throw new NoPermissionException(path + " " + arguments[0] + " <player>");
				}
				
				tp(player, target, arguments[1]);
				return;
			}
			
			//Check permissions now, since a sender is not tp'ing himself now.
			if(!sender.hasPermission(PERM_TP_OTHER))
			{
				throw new NoPermissionException(path + " <player> <player>");
			}
			
			if(player == null)
			{
				die("Player %v does not seem to be online.", arguments[0]);
			}
			
			if(target == null)
			{
				die("Player %v does not seem to be online.", arguments[1]);
			}
			
			if(target == player)
			{
				die("From-to are the same player.");
			}
			
			//Ignore policies completely, we are in a bypass code.
			if(player.teleport(target, TeleportCause.COMMAND))
			{
				f.n(target, "%v got teleported to you.", player.getName());
				f.n(player, "You got teleported to %v", target.getName());
				f.n(sender, "Teleported %v to %v.", player.getName(), target.getName());
			}
			else
			{
				f.n(sender, "Teleporting %v to %v was not possible.", player.getName(), target.getName());
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
				f.n(sender, "Successfully teleported %v.", player.getName());
				//TODO: By?
				f.n(player, "You have been teleported.");
			}
			else
			{
				f.e(sender, "Teleport was not possible.");
			}
		}
		else
		{
			exec(sender);
		}
	}
	
	private void tp(Player player, Player target, String playerName)
	{
		if(target == null)
		{
			die("Player %v does not seem to be online.", playerName);
		}
		
		if(target == player)
		{
			die("Try somebody else besides yourself.");
		}
		
		Policy policy = plugin.getTPPlayer(target).getTpReceivePolicy();
		
		if(policy == Policy.DENY)
		{
			f.n(target, "Player %v attempted to tp to you.", player.getName());
			die("Player %v does not allow teleportations.", target.getName());
		}
		else if(policy == Policy.ACCEPT)
		{
			if(player.teleport(target, TeleportCause.COMMAND))
			{
				f.n(target, "%v teleported to you.", player.getName());
				f.n(player, "You teleported to %v", target.getName());
			}
			else
			{
				f.n(player, "Teleport to %v was not possible.", target.getName());
			}
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] arguments)
	{
		List<String> suggestions = super.onTabComplete(sender, arguments);
		
		if(arguments.length == 1 && hasPermission(sender))
		{
			//TODO filter hidden players...
			suggestions.addAll(
					sender.getServer().getOnlinePlayers().stream()
						.map(Player::getName)
						.filter(e -> StringUtils.startsWithIgnoreCase(e, arguments[0]))
						.filter(e -> !e.equals(sender.getName()))
						.collect(Collectors.toList()));
		}
		
		return suggestions;
	}
	
	// LOCATION PARSING #######################################################
	
	private Location parseLocation(Location originLocation, String sx, String sy, String sz)
	{
		boolean sx_rel = sx.charAt(0) == '~';
		boolean sy_rel = sx.charAt(0) == '~';
		boolean sz_rel = sx.charAt(0) == '~';
		
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
			dx = parseD(sx);
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
			dy = parseD(sy);
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
			dz = parseD(sz);
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
		for(String permission : permissions)
		{
			if(sender.hasPermission(permission))
			{
				return true;
			}
		}
		
		return super.hasPermission(sender);
	}
	
	//Overwritten to set the permissions for the Bukkit call command permissions
	@Override
	public String getPermissions()
	{
		String permissions = super.getPermissions();
		String fill = permissions.isEmpty() ? "" : ".";
		
		return permissions  + fill + String.join(";", permissions);
	}
	
	@Override
	protected void init(Feedback f, String path, String permission)
	{
		String fill = permission.isEmpty() ? "" : ".";  
		
		PERM_TP = permission + fill + PERM_TP;
		PERM_TP_POS = permission + fill + PERM_TP_POS;
		PERM_TP_OTHER = permission + fill + PERM_TP_OTHER;
		PERM_TP_OTHER_POS = permission + fill + PERM_TP_OTHER_POS;
		
		permissions = new String[] { PERM_TP, PERM_TP_POS, PERM_TP_OTHER, PERM_TP_OTHER_POS };
		
		super.init(f, path, permission);
	}
}
