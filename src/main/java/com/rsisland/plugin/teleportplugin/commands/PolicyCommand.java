package com.rsisland.plugin.teleportplugin.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ecconia.rsisland.framework.cofami.Subcommand;
import com.rsisland.plugin.teleportplugin.TeleportPlugin;
import com.rsisland.plugin.teleportplugin.data.Policy;

public class PolicyCommand extends Subcommand
{
	private final TeleportPlugin plugin;
	
	public PolicyCommand(TeleportPlugin plugin)
	{
		super("policy");
		onlyPlayer();
		
		this.plugin = plugin;
	}
	
	@Override
	public void exec(CommandSender sender, String[] arguments)
	{
		Player player = getPlayer(sender);
		checkPermission(sender);
		
		if(arguments.length != 1)
		{
			f.e(sender, "Usage: ", path+" <policy>");
			f.n(sender, "%v - Default policy of this server.", "Reset");
			f.n(sender, "%v - Allow anyone to tp to you.", "Accept");
			f.n(sender, "%v - Player may only tp to you sending a request.", "Request");			
			f.n(sender, "%v - Deny anyone to tp to you.", "Deny");
			return;
		}
		
		String policyText = arguments[0].toLowerCase();
		Policy policy;
		
		if(policyText.equals("reset"))
		{
			policy = null;
		}
		else
		{
			policy = Policy.fromString(policyText);
			if(policy == null)
			{
				die("Could not parse %v to policy, try tabcomplete or %v to get a list of policies.", policyText, path);
			}
		}
		
		plugin.getTPPlayer(player).setTpReceivePolicy(policy);
		f.n(sender, "Changed policy to %v.", policyText);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		if(args.length == 1)
		{
			return Arrays.stream(new String[] {"accept", "request", "deny", "reset"}).filter(e -> StringUtils.startsWithIgnoreCase(e, args[0])).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}
}
