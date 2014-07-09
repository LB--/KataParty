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
				case "kataparty":
				{
					if(args.length == 0)
					{
						PluginDescriptionFile d = inst.getDescription();
						sender.sendMessage(d.getName()+" v"+d.getVersion()+" by "+d.getAuthors().get(0));
						sender.sendMessage("For help, use /help "+d.getName()+" [page-#]");
						return true;
					}
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
						inst.guis.put(player.getUniqueId(), KataParty.GuiType.LIST);
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
							sender.sendMessage("No KataParty named "+args[0]);
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
							sender.sendMessage("You are not in any KataParty");
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
							sender.sendMessage("You are not in any KataParty");
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
							sender.sendMessage("You do not have permission to disband your KataParty");
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
							sender.sendMessage("No KataParty named "+args[0]);
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
						Party p = inst.findParty(args[0]);
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
						Party.Member m = inst.findMember(player.getUniqueId());
						if(!m.getParty().canTp())
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
								sender.sendMessage("Shared Inventory is disabled for this KataParty");
							}
						}
						else
						{
							sender.sendMessage("You are not in any KataParty");
						}
						return true;
					}
				} break;
			}
		}
		return false;
	}
}
