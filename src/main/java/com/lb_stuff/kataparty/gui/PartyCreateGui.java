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
		super(plugin, p, 1, plugin.getMessage("create-gui-title", pname));

		addButton(CREATE, pname, Material.NAME_TAG, new ArrayList<String>(){
		{
			add(inst.getMessage("create-create"));
			add(inst.getMessage("create-cancel"));
		}});
		addButton(TELEPORTS, inst.getMessage("manage-teleports-enabled"), Material.ENDER_PEARL, new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.teleport.disable"))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(TELEPORTS, 2);
		addButton(PVP, inst.getMessage("manage-pvp-disabled"), Material.STONE_SWORD/*Material.GOLD_SWORD*/, new ArrayList<String>(){
		{
			add(inst.getMessage("manage-click-to-change"));
		}});
		addButton(INVENTORY, inst.getMessage("manage-inventory-disabled"), Material.CHEST/*Material.ENDER_CHEST*/, new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.inventory.enable"))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		addButton(VISIBLE, inst.getMessage("manage-visibility-enabled"), Material.JACK_O_LANTERN/*Material.PUMPKIN*/, new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.hide"))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(VISIBLE, 2);
	}
	private PartyCreateGui(KataPartyPlugin plugin, Player p, String pname, Object unused)
	{
		super(plugin, p, 1, plugin.getMessage("create-name-taken", pname));
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		switch(slot)
		{
			case CREATE:
			{
				String pname = getButtonName(CREATE);
				if(inst.getParties().findParty(pname) != null)
				{
					new PartyCreateGui(inst, player, pname, null).show();
					return;
				}
				Party p = inst.getParties().add(pname, player);
				p.setTp(getButton(TELEPORTS) != 1);
				p.setPvp(getButton(PVP) != 1);
				if(getButton(INVENTORY) != 1)
				{
					p.enableInventory();
				}
				p.setVisible(getButton(VISIBLE) != 1);
				inst.getFilter().tellFilterPref(player);
				hide();
			} break;
			case TELEPORTS:
			{
				if(player.hasPermission("KataParty.teleport.disable"))
				{
					if(getButton(TELEPORTS) != 1)
					{
						setButton(TELEPORTS, 1);
						setButton(TELEPORTS, inst.getMessage("manage-teleports-disabled"));
					}
					else
					{
						setButton(TELEPORTS, 2);
						setButton(TELEPORTS, inst.getMessage("manage-teleports-enabled"));
					}
				}
			} break;
			case PVP:
			{
				if(getButton(PVP) != 1)
				{
					setButton(PVP, 1, Material.STONE_SWORD);
					setButton(PVP, inst.getMessage("manage-pvp-disabled"));
				}
				else
				{
					setButton(PVP, 2, Material.GOLD_SWORD);
					setButton(PVP, inst.getMessage("manage-pvp-enabled"));
				}
			} break;
			case INVENTORY:
			{
				if(player.hasPermission("KataParty.inventory.enable"))
				{
					if(getButton(INVENTORY) != 1)
					{
						setButton(INVENTORY, 1, Material.CHEST);
						setButton(INVENTORY, inst.getMessage("manage-inventory-disabled"));
					}
					else
					{
						setButton(INVENTORY, 2, Material.ENDER_CHEST);
						setButton(INVENTORY, inst.getMessage("manage-inventory-enabled"));
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
						setButton(VISIBLE, inst.getMessage("manage-visibility-disabled"));
					}
					else
					{
						setButton(VISIBLE, 2, Material.JACK_O_LANTERN);
						setButton(VISIBLE, inst.getMessage("manage-visibility-enabled"));
					}
				}
			} break;
			default: break;
		}
	}

	@Override
	protected void onClose()
	{
		if(inst.getParties().findMember(player.getUniqueId()) == null)
		{
			inst.tellMessage(player, "create-cancelled");
		}
	}
}
