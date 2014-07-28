package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class PartyTeleportGui extends PartyGui
{
	private static final int TICKET = 0;
	private class TicketButton extends GenericGuiButton
	{
		public TicketButton()
		{
			super(Material.NAME_TAG);
		}
		@Override
		public ItemStack display()
		{
			if(inst.getPartySet().contains(party))
			{
				setName(party.getName());
				return super.display();
			}
			hide();
			return null;
		}
	}
	private final IParty party;
	public PartyTeleportGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, 6, plugin.getMessage("teleport-gui-title"));
		party = p;
	}

	private class ListedMemberButton extends GenericGuiButton
	{
		private final IParty.IMember m;
		public ListedMemberButton(IParty.IMember m)
		{
			super(SkullGenerator.getPlayerSkull(m.getUuid()));
			this.m = m;
		}
		@Override
		public ItemStack display()
		{
			if(m.getParty() == party)
			{
				final OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
				final Player onp = offp.getPlayer();
				setName(offp.getName());
				setLore(new ArrayList<String>(){
				{
					add(inst.getMessage("members-rank", m.getRankName()));
					add(inst.getMessage("members-online", (offp.isOnline() && player.canSee(onp))));
					add(inst.getMessage("members-teleports", m.canTp()));
					if(offp.isOnline() && player.canSee(onp))
					{
						setName(onp.getDisplayName());
						add(inst.getMessage("members-alive", !onp.isDead()));
					}
				}});
				return super.display();
			}
			return null;
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(m.getParty() == party)
			{
				OfflinePlayer target = inst.getServer().getOfflinePlayer(m.getUuid());
				if(target.isOnline() && m.canTp() && player.canSee(target.getPlayer()))
				{
					player.teleport(target.getPlayer());
					hide();
					inst.tellMessage(player, "member-teleported-to", target.getPlayer().getDisplayName());
					inst.tellMessage(target.getPlayer(), "member-teleported-from", player.getDisplayName());
					return true;
				}
			}
			return false;
		}
	}
	private final TicketButton ticket = new TicketButton();
	@Override
	protected void onUpdate()
	{
		clearButtons();

		if(!inst.getPartySet().contains(party))
		{
			hide();
			return;
		}

		addButton(TICKET, ticket);

		int buttons = 0;
		for(final IParty.IMember m : party)
		{
			if(m.getUuid().equals(player.getUniqueId()))
			{
				continue;
			}
			final OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
			final Player onp = offp.getPlayer();
			addButton(++buttons, new ListedMemberButton(m));
		}
	}
}
