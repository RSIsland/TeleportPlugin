package com.rsisland.plugin.teleportplugin.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ecconia.rsisland.framework.cofami.GroupSubcommand;
import com.ecconia.rsisland.framework.cofami.Subcommand;
import com.rsisland.plugin.teleportplugin.Policy;
import com.rsisland.plugin.teleportplugin.TeleportPlugin;
import com.rsisland.plugin.teleportplugin.Utils;

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
		
		//Print usage
		if(hasPermission(sender))
		{
			die("Usage: %v and %v", path+" <player>", path+" policy");
		}
		else
		{
			die("Usage: %v", path+" policy");
		}
	}
	
	@Override
	protected void noMatch(CommandSender sender, String[] arguments) //Arguments are not a subcommand
	{
		Player player = getPlayer(sender);
		
		checkPermission(sender);
		
		//Handle players or similar.
		if(arguments.length != 1)
		{
			exec(sender);
			return;
		}
		
		String playerName = arguments[0];
		Player target = Utils.getPlayer(sender.getServer().getOnlinePlayers(), playerName);
		
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
			f.n(target, "Player %v attempted to tp to you.", sender.getName());
			die("Player %v does not allow teleportations.", target.getName());
		}
		else if(policy == Policy.ACCEPT)
		{
			player.teleport(target);
			f.n(target, "%v teleported to you.", player.getName());
			f.n(sender, "You teleported to %v", target.getName());
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
