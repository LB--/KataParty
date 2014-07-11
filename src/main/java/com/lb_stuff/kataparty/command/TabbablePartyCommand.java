package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.command.TabCompleter;

public abstract class TabbablePartyCommand extends PartyCommand implements TabCompleter
{
	public TabbablePartyCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}
}
