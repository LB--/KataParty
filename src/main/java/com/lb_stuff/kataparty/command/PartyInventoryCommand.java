package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PartyInventoryCommand extends PartyCommand
{
	public PartyInventoryCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 0)
			{
				Party.Member m = inst.getParties().findMember(player.getUniqueId());
				if(m != null)
				{
					Inventory i = m.getParty().getInventory();
					if(i != null)
					{
						player.openInventory(i);
					}
					else
					{
						inst.tellMessage(player, "shared-inventory-disabled");
					}
				}
				else
				{
					inst.tellMessage(player, "not-in-party");
				}
				return true;
			}
		}
		return false;
	}
}
