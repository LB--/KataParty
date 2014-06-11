package com.lb_stuff.kataparty;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter
{
	private KataParty inst;

	public Commands(KataParty instance)
	{
		inst = instance;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> ret = new ArrayList<>();
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			String cmdname = cmd.getName().toLowerCase();
			switch(cmdname)
			{
				case "kpjoin":
				{
					for(KataParty.Party p : inst.parties)
					{
						if(p.visible && p.name.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
						{
							ret.add(p.name);
						}
					}
				} break;
				case "kpclose":
				case "kpadmin":
				{
					for(KataParty.Party p : inst.parties)
					{
						if(p.name.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
						{
							ret.add(p.name);
						}
					}
				} break;
				case "kptp":
				{
					KataParty.Party.Member m = inst.findMember(player.getUniqueId());
					if(m != null)
					{
						for(KataParty.Party.Member o : m.getParty().members)
						{
							String name = inst.getServer().getPlayer(o.uuid).getName();
							if(o.tp && name.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
							{
								ret.add(name);
							}
						}
					}
				} break;
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
			String cmdname = cmd.getName().toLowerCase();
			switch(cmdname)
			{
				case "kataparty":
				{
					//
				} break;
				case "kpcreate":
				{
					if(args.length == 1)
					{
						KataParty.Party p;
						inst.parties.add(p = inst.new Party(args[0]));
						p.add(player.getUniqueId(), KataParty.Rank.OWNER);
						//
						return true;
					}
				} break;
				case "kplist":
				{
					//
				} break;
				case "kpjoin":
				{
					//
				} break;
				case "kpleave":
				{
					//
				} break;
				case "kpdisband":
				{
					//
				} break;
				case "kpclose":
				{
					//
				} break;
				case "kpmanage":
				{
					//
				} break;
				case "kpadmin":
				{
					//
				} break;
				case "kptp":
				{
					//
				} break;
				case "kpshare":
				{
					if(args.length == 0)
					{
						KataParty.Party.Member m = inst.findMember(player.getUniqueId());
						if(m != null)
						{
							Inventory i = m.getParty().inv;
							if(i != null)
							{
								player.openInventory(i);
							}
							else
							{
								sender.sendMessage("Shared Inventory is disabled for this KataParty");
							}
						}
						else
						{
							sender.sendMessage("You are not in any KataParty!");
						}
						return true;
					}
				} break;
			}
		}
		return false;
	}
}
