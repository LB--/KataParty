package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class PartyCreateGui extends PartyGui
{
	private static final int CREATE = 0;
	private static final int TELEPORTS = 2;
	private static final int PVP = 3;
	private static final int INVENTORY = 4;
	private static final int VISIBLE = 5;
	public PartyCreateGui(KataPartyPlugin plugin, Player p, String pname)
	{
		super(plugin, p, 1, "Create KataParty "+pname);

		addButton(CREATE, pname, Material.NAME_TAG, new ArrayList<String>(){
		{
			add("Click to create with these settings");
			add("Close this inventory screen to cancel");
		}});
		addButton(TELEPORTS, "Teleportation enabled", Material.ENDER_PEARL, new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.teleport.disable"))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		setButton(TELEPORTS, 2);
		addButton(PVP, "PvP disabled", Material.STONE_SWORD/*Material.GOLD_SWORD*/, new ArrayList<String>(){
		{
			add("Click to change");
		}});
		addButton(INVENTORY, "Shared inventory disabled", Material.CHEST/*Material.ENDER_CHEST*/, new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.inventory.enable"))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		addButton(VISIBLE, "Will be visible in list", Material.JACK_O_LANTERN/*Material.PUMPKIN*/, new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.hide"))
			{
				add("Click to change");
			}
			else
			{
				add("You cannot change this");
			}
		}});
		setButton(VISIBLE, 2);
	}
	private PartyCreateGui(KataPartyPlugin plugin, Player p, String pname, boolean unused)
	{
		super(plugin, p, 1, "Name taken: "+pname);
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		switch(slot)
		{
			case CREATE:
			{
				String pname = getButtonName(CREATE);
				if(inst.findParty(pname) != null)
				{
					new PartyCreateGui(inst, player, pname, false).show();
					return;
				}
				Party p = new Party(inst, pname);
				p.addMember(player.getUniqueId()).setRank(Party.Rank.ADMIN);
				p.setTp(getButton(TELEPORTS) != 1);
				p.setPvp(getButton(PVP) != 1);
				if(getButton(INVENTORY) != 1)
				{
					p.enableInventory();
				}
				p.setVisible(getButton(VISIBLE) != 1);
				inst.parties.add(p);
				hide();
			} break;
			case TELEPORTS:
			{
				if(player.hasPermission("KataParty.teleport.disable"))
				{
					if(getButton(TELEPORTS) != 1)
					{
						setButton(TELEPORTS, 1);
						setButton(TELEPORTS, "Teleportation disabled");
					}
					else
					{
						setButton(TELEPORTS, 2);
						setButton(TELEPORTS, "Teleportation enabled");
					}
				}
			} break;
			case PVP:
			{
				if(getButton(PVP) != 1)
				{
					setButton(PVP, 1, Material.STONE_SWORD);
					setButton(PVP, "PvP disabled");
				}
				else
				{
					setButton(PVP, 2, Material.GOLD_SWORD);
					setButton(PVP, "PvP enabled");
				}
			} break;
			case INVENTORY:
			{
				if(player.hasPermission("KataParty.inventory.enable"))
				{
					if(getButton(INVENTORY) != 1)
					{
						setButton(INVENTORY, 1, Material.CHEST);
						setButton(INVENTORY, "Shared inventory disabled");
					}
					else
					{
						setButton(INVENTORY, 2, Material.ENDER_CHEST);
						setButton(INVENTORY, "Shared inventory enabled");
					}
				}
			} break;
			case VISIBLE:
			{
				if(player.hasPermission("KataParty.hide"))
				{
					if(getButton(VISIBLE) != 1)
					{
						setButton(VISIBLE, 1, Material.PUMPKIN);
						setButton(VISIBLE, "Will not be visible in list");
					}
					else
					{
						setButton(VISIBLE, 2, Material.JACK_O_LANTERN);
						setButton(VISIBLE, "Will be visible in list");
					}
				}
			} break;
			default: break;
		}
	}
}
