package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyChatToggleCommand extends PartyCommand
{
	public PartyChatToggleCommand(KataPartyPlugin plugin)
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
				if(inst.getPartySet().findMember(player.getUniqueId()) != null)
				{
					inst.tellMessage(player, "chat-filtering-toggled");
					inst.getFilter().togglePref(player.getUniqueId());
				}
				else
				{
					inst.tellMessage(player, "not-in-party");
				}
				return true;
			}
		}
		return false;
	}
}
