package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;

public final class PartyRenameGui extends PartyGui
{
	private final IParty party;
	public PartyRenameGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, Bukkit.createInventory(null, InventoryType.ANVIL, plugin.getMessage("rename-gui-title")));
		party = p;
	}

	@Override
	protected void update()
	{
		clearButtons();

		if(!inst.getParties().contains(party))
		{
			hide();
			return;
		}

		addButton(0, party.getName(), Material.NAME_TAG, new ArrayList<String>(){
		{
			add(inst.getMessage("rename-name-requirements"));
		}});
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		update();
		if(getButtonName(0) == null)
		{
			return;
		}

		party.rename(getButtonName(2));
		hide();
	}
}
