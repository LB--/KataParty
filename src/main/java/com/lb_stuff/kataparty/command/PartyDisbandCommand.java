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
				Party.Member m = inst.getParties().findMember(player.getUniqueId());
				if(m == null)
				{
					inst.tellMessage(player, "not-in-party");
				}
				else if(m.getRank() == Party.Rank.ADMIN)
				{
					Party p = m.getParty();
					inst.getParties().remove(p, player);
				}
				else
				{
					inst.tellMessage(player, "missing-disband-permission");
				}
				return true;
			}
		}
		return false;
	}
}
