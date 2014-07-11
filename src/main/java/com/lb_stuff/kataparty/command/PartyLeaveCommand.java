package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyLeaveCommand extends PartyCommand
{
	public PartyLeaveCommand(KataPartyPlugin plugin)
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
				else
				{
					m.getParty().removeMember(m.getUuid());
				}
				return true;
			}
		}
		return false;
	}
}
