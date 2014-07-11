package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PartyTeleportGui extends PartyGui
{
	private static final int TICKET = 0;
	private final Party party;
	public PartyTeleportGui(KataPartyPlugin plugin, Player plr, Party p)
	{
		super(plugin, plr, 6, "Members in your KataParty");
		party = p;

		addButton(TICKET, party.getName(), Material.NAME_TAG, new ArrayList<String>());

		int buttons = 0;
		for(final Party.Member m : party)
		{
			if(m.getUuid().equals(player.getUniqueId()))
			{
				continue;
			}
			final OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
			final Player onp = offp.getPlayer();
			addButton(++buttons, new ItemStack(Material.SKULL_ITEM, 1, (short)3));
			setButton(buttons, offp.getName(), new ArrayList<String>(){
			{
				add("Rank: "+m.getRank());
				add("Online: "+(offp.isOnline() && player.canSee(onp)));
				add("Allows TP: "+m.canTp());
				if(offp.isOnline() && player.canSee(onp))
				{
					add("Alive: "+!onp.isDead());
				}
			}});
		}
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		Party p = inst.findParty(party.getName());
		if(p == null)
		{
			hide();
			return;
		}
		if(slot == TICKET)
		{
			new PartyTeleportGui(inst, player, party).show();
			return;
		}
		if(inst.findParty(party.getName()) == null)
		{
			hide();
			return;
		}
		Party.Member m = party.findMember(getButtonName(slot));
		if(m == null || m.getParty() != party)
		{
			new PartyTeleportGui(inst, player, party).show();
			return;
		}

		OfflinePlayer target = inst.getServer().getOfflinePlayer(m.getUuid());
		if(target.isOnline() && m.canTp() && (player).canSee(target.getPlayer()))
		{
			player.teleport(target.getPlayer());
			hide();
			player.sendMessage("[KataParty] You were teleported to "+target.getName());
			target.getPlayer().sendMessage("[KataParty] "+player.getName()+" telepoted to you");
		}
	}
}
