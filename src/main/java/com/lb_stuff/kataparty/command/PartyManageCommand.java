package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.gui.PartyManageGui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyManageCommand extends PartyCommand
{
	public PartyManageCommand(KataPartyPlugin plugin)
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
					new PartyManageGui(inst, player, m.getParty()).show();
				}
				return true;
			}
		}
		return false;
	}
}
