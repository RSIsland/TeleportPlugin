package com.rsisland.plugin.teleportplugin.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.ecconia.rsisland.framework.cofami.Feedback;
import com.ecconia.rsisland.framework.cofami.GroupSubcommand;
import com.ecconia.rsisland.framework.cofami.Subcommand;
import com.ecconia.rsisland.framework.cofami.exceptions.NoPermissionException;
import com.rsisland.plugin.teleportplugin.Policy;
import com.rsisland.plugin.teleportplugin.TeleportPlugin;

public class TeleportCommand extends GroupSubcommand
{
	private final TeleportPlugin plugin;
	
	private String[] permissions;
	private String PERM_TP = "tp";
	private String PERM_TP_OTHER = "othertp";
	
	public TeleportCommand(TeleportPlugin plugin, Subcommand... subcommands)
	{
		super("tp", subcommands);
		this.plugin = plugin;
	}
	
	@Override
	protected void exec(CommandSender sender) //No arguments
	{
		List<String> usages = new ArrayList<>();
		
		if(sender instanceof Player && sender.hasPermission(PERM_TP))
		{
			usages.add(path + " <player>");
		}
		
		if(sender.hasPermission(PERM_TP_OTHER))
		{
			usages.add(path + " <player> <player>");
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
		else if(count == 2)
		{
			Player player = plugin.getUtils().getPlayer(sender, arguments[0]);
			Player target = plugin.getUtils().getPlayer(sender, arguments[1]);
			
			boolean canTpOthers = sender.hasPermission(PERM_TP_OTHER);
			
			if(player == sender)
			{
				if(!(sender.hasPermission(PERM_TP) || canTpOthers))
				{
					throw new NoPermissionException(path + " " + arguments[0] + " <player>");
				}
				else
				{
					tp(player, target, arguments[1]);
					return;
				}
			}
			
			//Check permissions now, since a sender is not tp'ing himself now.
			if(!canTpOthers)
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
				//TODO by?
				f.n(player, "You got teleported to %v", target.getName());
				
				if(sender != target)
				{
					f.n(target, "%v got teleported to you.", player.getName());
					f.n(sender, "Teleported %v to %v.", player.getName(), target.getName());
				}
				else
				{
					f.n(sender, "Teleported %v to you.", player.getName());
				}
			}
			else
			{
				f.n(sender, "Teleporting %v to %v was not possible.", player.getName(), target.getName());
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
	
	//This methods gets called when no subcommand matches.
	@Override
	protected List<String> onTabCompleteNoMatch(CommandSender sender, String[] arguments)
	{
		List<String> completions = new ArrayList<>();
		
		//Only add player completions when its the first or second argument for:
		//tp <player>
		//tp <player> <player>
		
		int count = arguments.length;
		
		if(count == 1 || count == 2)
		{
			boolean normalTP = sender.hasPermission(PERM_TP);
			boolean otherTP = sender.hasPermission(PERM_TP_OTHER);
			
			if(otherTP || (count == 1 && (sender instanceof Player) && normalTP))
			{
				completions.addAll(new ArrayList<String>(plugin.getUtils().getPlayerNames(
						sender, arguments[count-1], count == 2 ? arguments[0] : (!normalTP && otherTP ? null : sender.getName()))));
			}
		}
		
		//This contains all subcommand starting with the first argument, but only when there only is one argument.
		completions.addAll(super.onTabCompleteNoMatch(sender, arguments));
		
		return completions;
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
		String fill = permissions.isEmpty() ? "" : ";";
		
		return permissions  + fill + String.join(";", this.permissions);
	}
	
	@Override
	protected void init(Feedback f, String path, String permission)
	{
		PERM_TP = permission + name + "." + PERM_TP;
		PERM_TP_OTHER = permission + name + "." + PERM_TP_OTHER;
		
		permissions = new String[] { PERM_TP, PERM_TP_OTHER };
		
		super.init(f, path, permission);
	}
}
