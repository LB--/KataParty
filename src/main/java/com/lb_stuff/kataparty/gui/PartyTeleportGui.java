package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class PartyTeleportGui extends PartyGui
{
	private static final int TICKET = 0;
	private final IParty party;
	public PartyTeleportGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, 6, plugin.getMessage("teleport-gui-title"));
		party = p;
	}

	@Override
	protected void update()
	{
		clearButtons();

		if(!inst.getPartySet().contains(party))
		{
			hide();
			return;
		}

		addButton(TICKET, party.getName(), Material.NAME_TAG, new ArrayList<String>());

		int buttons = 0;
		for(final IParty.IMember m : party)
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
				add(inst.getMessage("members-rank", m.getRankName()));
				add(inst.getMessage("members-online", (offp.isOnline() && player.canSee(onp))));
				add(inst.getMessage("members-teleports", m.canTp()));
				if(offp.isOnline() && player.canSee(onp))
				{
					add(inst.getMessage("members-alive", !onp.isDead()));
				}
			}});
		}
	}

	@Override
	protected void onButton(int slot, ClickType click)
	{
		if(getButtonName(TICKET) == null)
		{
			return;
		}

		if(slot == TICKET)
		{
			return;
		}
		if(inst.getPartySet().findParty(party.getName()) == null)
		{
			hide();
			return;
		}
		IParty.IMember m = party.findMember(getButtonName(slot));
		if(m == null || m.getParty() != party)
		{
			return;
		}

		OfflinePlayer target = inst.getServer().getOfflinePlayer(m.getUuid());
		if(target.isOnline() && m.canTp() && (player).canSee(target.getPlayer()))
		{
			player.teleport(target.getPlayer());
			hide();
			inst.tellMessage(player, "member-teleported-to", target.getPlayer().getDisplayName());
			inst.tellMessage(target.getPlayer(), "member-teleported-from", player.getDisplayName());
		}
	}
}
