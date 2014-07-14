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
					ms.togglePref();
					inst.tellMessage(player, "chat-filtering-toggled");
					switch(ms.getPref())
					{
						case PREFER_PARTY:
						{
							inst.tellMessage(player, "chat-filtering-party", inst.getFilterSwap());
						} break;
						case PREFER_GLOBAL:
						{
							inst.tellMessage(player, "chat-filtering-global", inst.getFilterSwap());
						} break;
					}
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
