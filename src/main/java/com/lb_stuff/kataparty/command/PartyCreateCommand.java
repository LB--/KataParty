package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCreateCommand implements CommandExecutor
{
	private KataPartyPlugin inst;
	public PartyCreateCommand(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 1)
			{
				if(inst.findParty(args[0]) != null)
				{
					sender.sendMessage("[KataParty] There is already a KataParty named "+args[0]);
				}
				else
				{
					player.openInventory(inst.partyCreate(player, args[0]));
				}
				return true;
			}
		}
		return false;
	}
}
