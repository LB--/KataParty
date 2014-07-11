package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyDisbandCommand extends PartyCommand
{
	public PartyDisbandCommand(KataPartyPlugin plugin)
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
				if(m == null)
				{
					sender.sendMessage("[KataParty] You are not in any KataParty");
				}
				else if(m.getRank() == Party.Rank.ADMIN)
				{
					Party p = m.getParty();
					inst.parties.remove(p);
					p.disband();
					p.disableInventory(player);
				}
				else
				{
					sender.sendMessage("[KataParty] You do not have permission to disband your KataParty");
				}
				return true;
			}
		}
		return false;
	}
}
