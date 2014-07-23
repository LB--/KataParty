package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.gui.PartyCreateGui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCreateCommand extends PartyCommand
{
	public PartyCreateCommand(KataPartyPlugin plugin)
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
				if(inst.getPartySet().findParty(args[0]) != null)
				{
					inst.tellMessage(player, "party-already-exists", args[0]);
				}
				else
				{
					new PartyCreateGui(inst, player, args[0]).show();
				}
				return true;
			}
		}
		return false;
	}
}
