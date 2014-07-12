package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import static com.lb_stuff.kataparty.PartySet.MemberSettings;

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
				MemberSettings ms = inst.getParties().getSettings(player.getUniqueId());
				if(ms != null)
				{
					ms.setPartyPreferred( ! ms.isPartyPreferred());
					sender.sendMessage("[KataParty] Chat filtering settings toggled");
					if(ms.isPartyPreferred())
					{
						sender.sendMessage("[KataParty] You talk in party chat, start message with ! to speak globally");
					}
					else
					{
						sender.sendMessage("[KataParty] You talk in global chat, start message with ! to speak in party");
					}
				}
				else
				{
					sender.sendMessage("[KataParty] You are not in any KataParty");
				}
				return true;
			}
		}
		return false;
	}
}
