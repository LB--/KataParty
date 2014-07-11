package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyCloseCommand extends PartyAdminCommand
{
	public PartyCloseCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 1)
			{
				Party p = inst.findParty(args[0]);
				if(p == null)
				{
					sender.sendMessage("[KataParty] No KataParty named "+args[0]);
				}
				else
				{
					inst.parties.remove(p);
					p.disband();
					p.disableInventory(player);
				}
				return true;
			}
		}
		return false;
	}
}
