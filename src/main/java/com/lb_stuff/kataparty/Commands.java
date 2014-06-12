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
						if(inst.findParty(args[0]) != null)
						{
							sender.sendMessage("There is already a KataParty named "+args[0]);
						}
						else
						{
							player.openInventory(inst.partyCreate(player, args[0]));
						}
						return true;
					}
				} break;
				case "kplist":
				{
					if(args.length == 0)
					{
						inst.guis.put(player, KataParty.GuiType.LIST);
						player.openInventory(inst.partyList(player));
						return true;
					}
				} break;
				case "kpjoin":
				{
					if(args.length == 1)
					{
						KataParty.Party p = inst.findParty(args[0]);
						if(p == null)
						{
							sender.sendMessage("No KataParty named "+args[0]);
						}
						else
						{
							p.add(player.getUniqueId(), KataParty.Rank.MEMBER);
						}
						return true;
					}
				} break;
				case "kpleave":
				{
					if(args.length == 0)
					{
						KataParty.Party.Member m = inst.findMember(player.getUniqueId());
						if(m == null)
						{
							sender.sendMessage("You are not in any KataParty");
						}
						else
						{
							m.getParty().remove(m.uuid);
							sender.sendMessage("You left your KataParty");
						}
						return true;
					}
				} break;
				case "kpdisband":
				{
					if(args.length == 0)
					{
						KataParty.Party.Member m = inst.findMember(player.getUniqueId());
						if(m == null)
						{
							sender.sendMessage("You are not in any KataParty");
						}
						else if(m.rank == KataParty.Rank.ADMIN)
						{
							KataParty.Party p = m.getParty();
							inst.parties.remove(p);
							for(KataParty.Party.Member mem : p.members)
							{
								Player plr = inst.getServer().getPlayer(mem.uuid);
								if(plr != null)
								{
									plr.sendMessage("Your KataParty was disbanded");
								}
							}
							p.disableInventory(player);
						}
						else
						{
							sender.sendMessage("You do not have permission to disband your KataParty");
						}
						return true;
					}
				} break;
				case "kpclose":
				{
					if(args.length == 1)
					{
						KataParty.Party p = inst.findParty(args[0]);
						if(p == null)
						{
							sender.sendMessage("No KataParty named "+args[0]);
						}
						else
						{
							inst.parties.remove(p);
							for(KataParty.Party.Member m : p.members)
							{
								Player plr = inst.getServer().getPlayer(m.uuid);
								if(plr != null)
								{
									plr.sendMessage("Your KataParty was closed");
								}
							}
							p.disableInventory(player);
						}
						return true;
					}
				} break;
				case "kpmanage":
				{
					if(args.length == 0)
					{
						KataParty.Party.Member m = inst.findMember(player.getUniqueId());
						if(m == null)
						{
							sender.sendMessage("You are not in any KataParty!");
						}
						else
						{
							player.openInventory(inst.partyManage(m.getParty(), player));
						}
						return true;
					}
				} break;
				case "kpadmin":
				{
					if(args.length == 1)
					{
						KataParty.Party p = inst.findParty(args[0]);
						if(p == null)
						{
							sender.sendMessage("No KataParty named "+args[0]);
						}
						else
						{
							player.openInventory(inst.partyManage(p, player));
						}
						return true;
					}
				} break;
				case "kptp":
				{
					if(args.length == 0)
					{
						KataParty.Party.Member m = inst.findMember(player.getUniqueId());
						if(!m.getParty().tp)
						{
							sender.sendMessage("Your KataParty does not allow teleportations");
						}
						else
						{
							player.openInventory(inst.partyTeleport(player));
						}
					}
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
