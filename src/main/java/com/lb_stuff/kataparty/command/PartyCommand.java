package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.command.CommandExecutor;

public abstract class PartyCommand implements CommandExecutor
{
	protected final KataPartyPlugin inst;
	public PartyCommand(KataPartyPlugin plugin)
	{
		inst = plugin;
	}
}
