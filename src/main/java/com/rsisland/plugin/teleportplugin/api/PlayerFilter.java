package com.rsisland.plugin.teleportplugin.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerFilter
{
	public abstract boolean test(CommandSender completer, Player test);
}
