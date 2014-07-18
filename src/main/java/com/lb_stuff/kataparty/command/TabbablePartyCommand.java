package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;

public abstract class TabbablePartyCommand extends PartyCommand implements TabCompleter, TabExecutor
{
	public TabbablePartyCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}
}
