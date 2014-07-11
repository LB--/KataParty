package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;
import com.lb_stuff.kataparty.gui.PartyTeleportGui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

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
			Party.Member m = inst.findMember(player.getUniqueId());
			if(m != null)
			{
				for(Party.Member o : m.getParty())
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
				Party.Member m = inst.findMember(player.getUniqueId());
				if(!m.getParty().canTp())
				{
					sender.sendMessage("[KataParty] Your KataParty does not allow teleportations");
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
						Party.Member t = inst.findMember(tp.getUniqueId());
						if(t != null && t.getParty() == m.getParty())
						{
							if(m.getParty().canTp() && t.canTp())
							{
								player.teleport(tp);
								sender.sendMessage("[KataParty] You were teleported to "+tp.getName());
								tp.sendMessage("[KataParty] "+player.getName()+" telepoted to you");
							}
							else
							{
								sender.sendMessage("[KataParty] You cannot teleport to "+tp.getName());
							}
						}
						else
						{
							sender.sendMessage("[KataParty] "+tp.getName()+" is not in your KataParty");
						}
					}
					else
					{
						sender.sendMessage("[KataParty] Could not find player \""+args[0]+"\"");
					}
				}
				return true;
			}
		}
		return false;
	}
}
