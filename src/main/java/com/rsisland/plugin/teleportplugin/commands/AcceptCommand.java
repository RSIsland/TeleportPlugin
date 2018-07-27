package com.rsisland.plugin.teleportplugin.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.ecconia.rsisland.framework.cofami.Subcommand;
import com.rsisland.plugin.teleportplugin.TeleportPlugin;
import com.rsisland.plugin.teleportplugin.data.TPPlayer;

public class AcceptCommand extends Subcommand
{
	private final TeleportPlugin plugin;
	
	public AcceptCommand(TeleportPlugin plugin)
	{
		super("tpa");
		this.plugin = plugin;
		onlyPlayer();
	}
	
	@Override
	public void exec(CommandSender sender, String[] arguments)
	{
		//Accepts a pending request incomming to the sender
		Player player = getPlayer(sender);
		TPPlayer tpPlayer = plugin.getTPPlayer(player);
		
		Set<Player> toTPPlayers;
		
		if(arguments.length == 0)
		{
			//TODO: Player may accept even the ones at the edge, instead of just the very recent ones.
			// Maybe add message if a request time'd out.
			// Alternative make an outter and inner timespan (the outter one for accept all)
			// If only one possible accept, time doesn't even matter, consider that too...
			toTPPlayers = new HashSet<>(tpPlayer.getRequestManager().getAll());
			
			if(toTPPlayers.isEmpty())
			{
				die("No recent teleportation request.");
			}
		}
		else
		{
			toTPPlayers = new HashSet<>();
			Set<String> whosThat = new HashSet<>();
			Set<String> didNotRequest = new HashSet<>();
			
			for(String playerName : arguments)
			{
				//TODO: Get all starting with and filter then?
				Player p = plugin.getUtils().getPlayer(sender, playerName);
				if(p == null)
				{
					whosThat.add(playerName);
					continue;
				}
				
				if(tpPlayer.getRequestManager().accept(p))
				{
					toTPPlayers.add(p);
				}
				else
				{
					didNotRequest.add(p.getName());
				}
			}
			
			printList(sender, whosThat, (whosThat.size() == 1 ? "is" : "are") + " not online", false);
			printList(sender, didNotRequest, "did not recently request to tp to you", false);
		}
		
		List<String> yepTPed = new ArrayList<>();
		List<String> nopTPed = new ArrayList<>();
		
		for(Player toTPPlayer : toTPPlayers)
		{
			if(toTPPlayer.teleport(player, TeleportCause.COMMAND))
			{
				yepTPed.add(toTPPlayer.getName());
				f.e(toTPPlayer, "Your teleportation request to %v got accepted.", player.getName());
			}
			else
			{
				nopTPed.add(toTPPlayer.getName());
				f.n(toTPPlayer, "Your teleportation request to %v got accepted, but failed.", player.getName());
			}
		}
		
		printList(sender, nopTPed, "could not be teleported to you", false);
		printList(sender, yepTPed, (yepTPed.size() == 1 ? "has" : "have") + " been teleported to you", true);
	}
	
	private void printList(CommandSender sender, Collection<String> list, String message, boolean good)
	{
		if(!list.isEmpty())
		{
			String messageFormat;
			if(list.size() == 1)
			{
				messageFormat = "Player %v " + message + ".";
			}
			else
			{
				String args = "%v";
				
				for(int i = 0; i < list.size()-2; i++)
				{
					args += ", %v";
				}

				if(list.size() > 1)
				{
					args += " and %v";
				}
				
				messageFormat = "Players" + args + " " + message + ".";
			}
			
			if(good)
			{
				f.n(sender, messageFormat, list.toArray(new Object[0]));
			}
			else
			{
				f.e(sender, messageFormat, list.toArray(new Object[0]));
			}
		}
	}
}
