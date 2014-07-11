package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;
import com.lb_stuff.kataparty.gui.PartyManageGui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyAdminCommand extends TabbablePartyCommand
{
	public PartyAdminCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		for(Party p : inst.parties)
		{
			if(p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
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
				Party p = inst.findParty(args[0]);
				if(p == null)
				{
					sender.sendMessage("[KataParty] No KataParty named "+args[0]);
				}
				else
				{
					new PartyManageGui(inst, player, p).show();
				}
				return true;
			}
		}
		return false;
	}
}
