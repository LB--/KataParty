package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyListCommand extends PartyCommand
{
	public PartyListCommand(KataPartyPlugin plugin)
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
				inst.guis.put(player.getUniqueId(), KataPartyPlugin.GuiType.LIST);
				player.openInventory(inst.partyList(player));
				return true;
			}
		}
		return false;
	}
}
