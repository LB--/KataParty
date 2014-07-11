package com.lb_stuff.kataparty;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.*;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter
{
	private KataPartyPlugin inst;

	public Commands(KataPartyPlugin instance)
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
					for(Party p : inst.parties)
					{
						if(p.isVisible() && p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
						{
							ret.add(p.getName());
						}
					}
				} break;
				case "kpclose":
				case "kpadmin":
				{
					for(Party p : inst.parties)
					{
						if(p.getName().toLowerCase().startsWith(args[args.length-1].toLowerCase()))
						{
							ret.add(p.getName());
						}
					}
				} break;
				case "kptp":
				{
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
				} break;
				default: break;
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
				case "kplist":
				{
					if(args.length == 0)
					{
						inst.guis.put(player.getUniqueId(), KataPartyPlugin.GuiType.LIST);
						player.openInventory(inst.partyList(player));
						return true;
					}
				} break;
				case "kpjoin":
				{
					if(args.length == 1)
					{
						Party p = inst.findParty(args[0]);
						if(p == null)
						{
							sender.sendMessage("[KataParty] No KataParty named "+args[0]);
						}
						else
						{
							p.addMember(player.getUniqueId());
						}
						return true;
					}
				} break;
				case "kpleave":
				{
					if(args.length == 0)
					{
						Party.Member m = inst.findMember(player.getUniqueId());
						if(m == null)
						{
							sender.sendMessage("[KataParty] You are not in any KataParty");
						}
						else
						{
							m.getParty().removeMember(m.getUuid());
						}
						return true;
					}
				} break;
				case "kpdisband":
				{
					if(args.length == 0)
					{
						Party.Member m = inst.findMember(player.getUniqueId());
						if(m == null)
						{
							sender.sendMessage("[KataParty] You are not in any KataParty");
						}
						else if(m.getRank() == Party.Rank.ADMIN)
						{
							Party p = m.getParty();
							inst.parties.remove(p);
							p.disband();
							p.disableInventory(player);
						}
						else
						{
							sender.sendMessage("[KataParty] You do not have permission to disband your KataParty");
						}
						return true;
					}
				} break;
				case "kpclose":
				{
					if(args.length == 1)
					{
						Party p = inst.findParty(args[0]);
						if(p == null)
						{
							sender.sendMessage("[KataParty] No KataParty named "+args[0]);
						}
						else
						{
							inst.parties.remove(p);
							p.disband();
							p.disableInventory(player);
						}
						return true;
					}
				} break;
				case "kpmanage":
				{
					if(args.length == 0)
					{
						Party.Member m = inst.findMember(player.getUniqueId());
						if(m == null)
						{
							sender.sendMessage("[KataParty] You are not in any KataParty!");
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
						Party p = inst.findParty(args[0]);
						if(p == null)
						{
							sender.sendMessage("[KataParty] No KataParty named "+args[0]);
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
					if(args.length == 0 || args.length == 1)
					{
						Party.Member m = inst.findMember(player.getUniqueId());
						if(!m.getParty().canTp())
						{
							sender.sendMessage("[KataParty] Your KataParty does not allow teleportations");
						}
						else if(args.length == 0)
						{
							player.openInventory(inst.partyTeleport(player));
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
				} break;
				case "kpshare":
				{
					if(args.length == 0)
					{
						Party.Member m = inst.findMember(player.getUniqueId());
						if(m != null)
						{
							Inventory i = m.getParty().getInventory();
							if(i != null)
							{
								player.openInventory(i);
							}
							else
							{
								sender.sendMessage("[KataParty] Shared Inventory is disabled for this KataParty");
							}
						}
						else
						{
							sender.sendMessage("[KataParty] You are not in any KataParty");
						}
						return true;
					}
				} break;
				case "kptoggle":
				{
					if(args.length == 0)
					{
						KataPartyPlugin.MemberSettings ms = inst.partiers.get(player.getUniqueId());
						if(ms != null)
						{
							ms.talkparty = !ms.talkparty;
							sender.sendMessage("[KataParty] Chat filtering settings toggled");
							if(ms.talkparty)
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
			}
		}
		return false;
	}
}
