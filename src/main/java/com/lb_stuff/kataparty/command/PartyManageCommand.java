package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;
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
				Party.Member m = inst.getParties().findMember(player.getUniqueId());
				if(m == null)
				{
					sender.sendMessage("[KataParty] You are not in any KataParty!");
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
