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
				Party.Member m = inst.findMember(player.getUniqueId());
				if(m != null)
				{
					Inventory i = m.getParty().getInventory();
					if(i != null)
					{
						player.openInventory(i);
					}
					else
					{
						sender.sendMessage("[KataParty] Shared Inventory is disabled for this KataParty");
					}
				}
				else
				{
					sender.sendMessage("[KataParty] You are not in any KataParty");
				}
				return true;
			}
		}
		return false;
	}
}
