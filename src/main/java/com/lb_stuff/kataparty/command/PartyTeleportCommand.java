package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.gui.PartyTeleportGui;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyTeleportCommand extends TabbablePartyCommand
{
	public PartyTeleportCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		if(args.length == 1 && sender instanceof Player)
		{
			Player player = (Player)sender;
			IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
			if(m != null)
			{
				for(IParty.IMember o : m.getParty())
				{
					OfflinePlayer offp = inst.getServer().getOfflinePlayer(o.getUuid());
					Player onp = offp.getPlayer();
					if(offp.isOnline() && player.canSee(onp))
					{
						String name = onp.getName();
						if(o.canTp() && name.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
						{
							ret.add(name);
						}
					}
				}
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
			if(args.length == 0 || args.length == 1)
			{
				IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
				if(!m.getParty().canTp())
				{
					inst.tellMessage(player, "party-teleports-disabled");
				}
				else if(args.length == 0)
				{
					new PartyTeleportGui(inst, player, m.getParty()).show();
				}
				else
				{
					Player tp = inst.getServer().getPlayer(args[0]);
					if(tp != null && player.canSee(tp))
					{
						IParty.IMember t = inst.getPartySet().findMember(tp.getUniqueId());
						if(t != null && t.getParty() == m.getParty())
						{
							if(m.getParty().canTp() && t.canTp())
							{
								player.teleport(tp);
								inst.tellMessage(player, "member-teleported-to", tp.getDisplayName());
								inst.tellMessage(tp, "member-teleported-from", player.getDisplayName());
							}
							else
							{
								inst.tellMessage(player, "member-teleport-disabled", tp.getDisplayName());
							}
						}
						else
						{
							inst.tellMessage(player, "player-not-member", tp.getDisplayName());
						}
					}
					else
					{
						inst.tellMessage(player, "player-not-found", args[0]);
					}
				}
				return true;
			}
		}
		return false;
	}
}
