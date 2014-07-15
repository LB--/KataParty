package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyJoinCommand extends TabbablePartyCommand
{
	public PartyJoinCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		for(Party p : inst.getParties())
		{
			if(p.isVisible() && p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
			{
				ret.add(p.getName());
			}
		}
		return ret;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 1)
			{
				Party p = inst.getParties().findParty(args[0]);
				if(p == null)
				{
					inst.tellMessage(player, "party-does-not-exist", args[0]);
				}
				else
				{
					Party.Member m = inst.getParties().findMember(player.getUniqueId());
					if(m != null)
					{
						if(m.getParty() != p)
						{
							p.addMember(player.getUniqueId());
						}
						else
						{
							inst.tellMessage(player, "join-already-member", args[0]);
						}
					}
				}
				return true;
			}
		}
		return false;
	}
}
