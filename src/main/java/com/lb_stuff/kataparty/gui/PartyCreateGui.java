package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public final class PartyCreateGui extends PartyGui
{
	private boolean getDefault(String path)
	{
		return inst.getConfig().getBoolean("party-defaults."+path);
	}

	private static final int TICKET = 0;
	private static final int TELEPORTS = 2;
	private static final int PVP = 3;
	private static final int INVENTORY = 4;
	private static final int VISIBLE = 5;
	private static final int INVITES = 6;
	public PartyCreateGui(KataPartyPlugin plugin, Player p, String pname)
	{
		super(plugin, p, 1, plugin.getMessage("create-gui-title", pname));

		addButton(TICKET, pname, Material.NAME_TAG, new ArrayList<String>(){
		{
			add(inst.getMessage("create-create"));
			add(inst.getMessage("create-cancel"));
		}});
		addButton(TELEPORTS, inst.getMessage(getDefault("teleports")? "manage-teleports-enabled" : "manage-teleports-disabled"), Material.ENDER_PEARL, new ArrayList<String>(){
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
		setButton(TELEPORTS, (getDefault("teleports")? 2 : 1));
		addButton(PVP, inst.getMessage(getDefault("pvp")? "manage-pvp-enabled" : "manage-pvp-disabled"), (getDefault("pvp")? Material.GOLD_SWORD : Material.STONE_SWORD), new ArrayList<String>(){
		{
			add(inst.getMessage("manage-click-to-change"));
		}});
		setButton(PVP, (getDefault("pvp")? 2 : 1));
		addButton(INVENTORY, inst.getMessage(getDefault("inventory")? "manage-inventory-enabled" : "manage-inventory-disabled"), (getDefault("inventory")? Material.ENDER_CHEST : Material.CHEST), new ArrayList<String>(){
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
		setButton(INVENTORY, (getDefault("inventory")? 2 : 1));
		addButton(VISIBLE, inst.getMessage(getDefault("visible")? "manage-visibility-enabled" : "manage-visibility-disabled"), (getDefault("visible")? Material.JACK_O_LANTERN : Material.PUMPKIN), new ArrayList<String>(){
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
		setButton(VISIBLE, (getDefault("visible")? 2 : 1));
		addButton(INVITES, inst.getMessage(getDefault("invite-only")? "manage-invites-enabled" : "manage-invites-disabled"), (getDefault("invite-only")? Material.IRON_DOOR : Material.WOODEN_DOOR), new ArrayList<String>(){
		{
			if(player.hasPermission("KataParty.invite.enforce"))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(INVITES, (getDefault("invite-only")? 2 : 1));
	}

	@Override
	protected void update()
	{
		String pname = getButtonName(TICKET);
		if(pname != null && inst.getParties().findParty(pname) != null)
		{
			clearButtons();
			rename(inst.getMessage("create-name-taken", pname));
		}
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		switch(slot)
		{
			case TICKET:
			{
				update();
				String pname = getButtonName(TICKET);
				if(pname == null)
				{
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
			case INVITES:
			{
				if(player.hasPermission("KataParty.invite.enforce"))
				{
					if(getButton(INVITES) != 1)
					{
						setButton(INVITES, 1, Material.WOODEN_DOOR);
						setButton(INVITES, inst.getMessage("manage-invites-disabled"));
					}
					else
					{
						setButton(INVITES, 2, Material.IRON_DOOR);
						setButton(INVITES, inst.getMessage("manage-invites-enabled"));
					}
				}
			}
			default: break;
		}
	}

	@Override
	protected void onClose()
	{
		String pname = getButtonName(TICKET);
		if(pname != null)
		{
			Party.Member m = inst.getParties().findMember(player.getUniqueId());
			if(m == null || !m.getParty().getName().equals(pname))
			{
				inst.tellMessage(player, "create-cancelled");
			}
		}
	}
}
