package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.Perms;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyJoinCommand extends TabbablePartyCommand
{
	public PartyJoinCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		if(args.length == 1)
		{
			for(IParty p : inst.getPartySet())
			{
				if(p.isVisible() && p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
				{
					ret.add(p.getName());
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
			if(args.length == 1)
			{
				IParty p = inst.getPartySet().findParty(args[0]);
				if(p == null)
				{
					inst.tellMessage(player, "party-does-not-exist", args[0]);
				}
				else
				{
					IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
					if(m == null || m.getParty() != p)
					{
						if(!p.isInviteOnly() || Perms.arbiter(player))
						{
							p.newMember(new MemberSettings(player.getUniqueId()), PartyMemberJoinEvent.Reason.VOLUNTARY);
						}
						else
						{
							inst.tellMessage(player, "party-join-invite-only-fail", args[0]);
						}
					}
					else
					{
						inst.tellMessage(player, "join-already-member", args[0]);
					}
				}
				return true;
			}
		}
		return false;
	}
}
