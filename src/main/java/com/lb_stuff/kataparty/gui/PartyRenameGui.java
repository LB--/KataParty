package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;

public final class PartyRenameGui extends PartyGui
{
	private final IParty party;
	public PartyRenameGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, Bukkit.createInventory(null, InventoryType.ANVIL, plugin.getMessage("rename-gui-title")));
		party = p;
	}

	@Override
	protected void onUpdate()
	{
		clearButtons();

		if(!inst.getPartySet().contains(party))
		{
			hide();
			return;
		}

		addButton(0, new GenericGuiButton(party.getName(), Material.NAME_TAG, inst.getMessage("rename-name-requirements"))
		{
			@Override
			public boolean onClick(ClickType click)
			{
				update();
				if(getInventory().getItem(0) == null || getInventory().getItem(0).getItemMeta().getDisplayName() == null)
				{
					return false;
				}

				party.setName(getInventory().getItem(2).getItemMeta().getDisplayName());
				hide();
				return true;
			}
		});
	}
}
