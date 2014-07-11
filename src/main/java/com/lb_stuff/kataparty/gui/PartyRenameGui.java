package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;

public class PartyRenameGui extends PartyGui
{
	private final Party party;
	public PartyRenameGui(KataPartyPlugin plugin, Player plr, Party p)
	{
		super(plugin, plr, Bukkit.createInventory(null, InventoryType.ANVIL, "Rename Party"));
		party = p;

		addButton(0, party.getName(), Material.NAME_TAG, new ArrayList<String>(){
		{
			add("Party name must be unique and not contain spaces");
		}});
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		if(inst.findParty(party.getName()) == null)
		{
			hide();
			return;
		}
		party.rename(getButtonName(2));
		hide();
	}
}
