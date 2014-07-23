package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

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
				IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
				if(m == null)
				{
					inst.tellMessage(player, "not-in-party");
				}
				else
				{
					m.getParty().removeMember(m.getUuid(), PartyMemberLeaveEvent.Reason.VOLUNTARY);
				}
				return true;
			}
		}
		return false;
	}
}
