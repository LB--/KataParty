package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

public class PartyManageGui extends PartyGui
{
	private static final int LEAVE = 0;
	private static final int MEMBERS = 1;
	private static final int TELEPORTS = 2;
	private static final int PVP = 3;
	private static final int INVENTORY = 4;
	private static final int VISIBLE = 5;
	private static final int DISBAND = 9;
	private static final int TPALL = 10;
	private static final int SELFTP = 11;
	private final Party party;
	public PartyManageGui(KataPartyPlugin plugin, Player plr, Party p)
	{
		super(plugin, plr, 1, p.getName()+" Settings");
		party = p;

		final Party.Member mt = inst.findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		addButton(LEAVE, party.getName(), Material.NAME_TAG, new ArrayList<String>(){
		{
			if(isMember)
			{
				add("Your rank: "+mt.getRank());
				add("Left click to leave this KataParty");
			}
			else
			{
				add("You are not a member of this KataParty");
			}
			if(isAdmin || isPartyAdmin)
			{
				add("Right click to rename this KataParty");
			}
			if(isAdmin)
			{
				add("You are managing this KataParty as a server admin");
			}
		}});
		int online = 0, mods = 0, onmods = 0, admins = 0, onadmins = 0;
		for(Party.Member mem : party)
		{
			if(mem.getRank().equals(Party.Rank.MODERATOR))
			{
				++mods;
			}
			else if(mem.getRank().equals(Party.Rank.ADMIN))
			{
				++admins;
			}
			if(inst.getServer().getPlayer(mem.getUuid()) != null)
			{
				++online;
				if(mem.getRank().equals(Party.Rank.MODERATOR))
				{
					++onmods;
				}
				else if(mem.getRank().equals(Party.Rank.ADMIN))
				{
					++onadmins;
				}
			}
		}
		final int online_ = online;
		final int mods_ = mods;
		final int onmods_ = onmods;
		final int admins_ = admins;
		final int onadmins_ = onadmins;
		addButton(MEMBERS, new ItemStack(Material.SKULL_ITEM, party.numMembers(), (short)3));
		setButton(MEMBERS, "Members (submenu)", new ArrayList<String>(){
		{
			add(online_+"/"+party.numMembers()+" online");
			add(onmods_+"/"+mods_+" moderators online");
			add(onadmins_+"/"+admins_+" admins online");
		}});
		addButton(TELEPORTS, "Teleportation "+(party.canTp()? "enabled" : "disabled"), Material.ENDER_PEARL, new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.teleport.disable") && isPartyMod))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		setButton(TELEPORTS, (party.canTp()? 2 : 1));
		addButton(PVP, "PvP "+(party.canPvp()? "enabled" : "disabled"), (party.canPvp()? Material.GOLD_SWORD : Material.STONE_SWORD), new ArrayList<String>(){
		{
			if(isAdmin || isPartyMod)
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		setButton(PVP, (party.canPvp()? 2 : 1));
		addButton(INVENTORY, "Shared inventory "+(party.getInventory() != null? "enabled" : "disabled"), (party.getInventory() != null? Material.ENDER_CHEST : Material.CHEST), new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.inventory.enable") && isPartyMod))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		setButton(INVENTORY, (party.getInventory() != null? 2 : 1));
		addButton(VISIBLE, "Will be visible in list", (party.isVisible()? Material.JACK_O_LANTERN : Material.PUMPKIN), new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.hide") && isPartyAdmin))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		setButton(VISIBLE, (party.isVisible()? 2 : 1));
		addButton(DISBAND, (isMember? "Disband your KataParty" : "Close this KataParty"), Material.TNT, new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.disband") && isPartyAdmin))
			{
				add("Click to use");
			}
			else
			{
				add("You cannot use this");
			}
		}});
		addButton(TPALL, "Teleport all players to yourself", Material.ENDER_PORTAL_FRAME, new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.teleport.do") && isPartyAdmin))
			{
				add("Click to use");
			}
			else
			{
				add("You cannot use this");
			}
		}});
		if(isMember)
		{
			addButton(SELFTP, "Members are"+(mt.canTp()? "" : " not")+" allowed to teleport to you", Material.EYE_OF_ENDER, new ArrayList<String>(){
			{
				if(player.hasPermission("KataParty.teleport.disallow"))
				{
					add("Click to change");
				}
				else
				{
					add("You cannot change this");
				}
			}});
		}
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		if(inst.findParty(party.getName()) == null)
		{
			hide();
			return;
		}

		final Party.Member mt = inst.findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		switch(slot)
		{
			case LEAVE:
			{
				switch(click)
				{
					case LEFT:
					{
						if(isMember)
						{
							party.removeMember(player.getUniqueId());
							hide();
						}
					} break;
					case RIGHT:
					{
						if(isAdmin || isPartyAdmin)
						{
							new PartyRenameGui(inst, player, party).show();
						}
					} break;
					default: break;
				}
			} break;
			case MEMBERS:
			{
				new PartyMembersGui(inst, player, party).show();
			} break;
			case TELEPORTS:
			{
				if(isAdmin || (player.hasPermission("KataParty.teleport.disable") && isPartyMod))
				{
					party.setTp(!party.canTp());
					new PartyManageGui(inst, player, party).show();
				}
			} break;
			case PVP:
			{
				if(isAdmin || isPartyMod)
				{
					party.setPvp(!party.canPvp());
					new PartyManageGui(inst, player, party).show();
				}
			} break;
			case INVENTORY:
			{
				if(isAdmin || (player.hasPermission("KataParty.inventory.enable") && isPartyMod))
				{
					if(party.getInventory() == null)
					{
						party.enableInventory();
					}
					else
					{
						party.disableInventory(player);
					}
					new PartyManageGui(inst, player, party).show();
				}
			} break;
			case VISIBLE:
			{
				if(isAdmin || (player.hasPermission("KataParty.hide") && isPartyAdmin))
				{
					party.setVisible(!party.isVisible());
					new PartyManageGui(inst, player, party).show();
				}
			} break;
			case DISBAND:
			{
				if(isAdmin || (player.hasPermission("KataParty.disband") && isPartyAdmin))
				{
					inst.parties.remove(party);
					party.disband();
					party.disableInventory(player);
					hide();
				}
			} break;
			case TPALL:
			{
				if(isAdmin || (player.hasPermission("KataParty.teleport.do") && isPartyAdmin))
				{
					for(Party.Member mem : party)
					{
						if(!mem.getUuid().equals(player.getUniqueId()) && mem.canTp())
						{
							OfflinePlayer offp = inst.getServer().getOfflinePlayer(mem.getUuid());
							if(offp.isOnline())
							{
								Player onp = offp.getPlayer();
								onp.setNoDamageTicks(20*5); //inulnerable for 5 seconds
								onp.teleport(player);
								onp.sendMessage("[KataParty] You were teleported to "+player.getName());
							}
						}
					}
					hide();
				}
			} break;
			case SELFTP:
			{
				if(isMember && player.hasPermission("KataParty.teleport.disallow"))
				{
					mt.setTp(!mt.canTp());
					new PartyManageGui(inst, player, party).show();
				}
			} break;
			default: break;
		}
	}
}
