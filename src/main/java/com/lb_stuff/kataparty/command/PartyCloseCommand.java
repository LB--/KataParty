package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
				IParty p = inst.getPartySet().findParty(args[0]);
				if(p == null)
				{
					inst.tellMessage(player, "party-does-not-exist", args[0]);
				}
				else
				{
					inst.getPartySet().remove(p, PartyDisbandEvent.Reason.SERVER_ADMIN_CLOSE, player);
				}
				return true;
			}
		}
		return false;
	}
}
