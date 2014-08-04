package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberTeleportEvent;

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
		private final IParty.IMember tm;
		public ListedMemberButton(IParty.IMember m)
		{
			super(SkullGenerator.getPlayerSkull(m.getUuid()));
			this.tm = m;
		}
		@Override
		public ItemStack display()
		{
			if(tm.getParty() == party)
			{
				final OfflinePlayer offp = inst.getServer().getOfflinePlayer(tm.getUuid());
				final Player onp = offp.getPlayer();
				setName(offp.getName());
				setLore(new ArrayList<String>(){
				{
					add(inst.getMessage("members-rank", tm.getRankName()));
					add(inst.getMessage("members-online", (offp.isOnline() && player.canSee(onp))));
					add(inst.getMessage("members-teleports", tm.canTp()));
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
			if(tm.getParty() == party)
			{
				IParty.IMember sm = inst.getPartySet().findMember(player.getUniqueId());
				if(sm == null || sm.getParty() != tm.getParty())
				{
					update();
					return false;
				}
				OfflinePlayer target = inst.getServer().getOfflinePlayer(tm.getUuid());
				PartyMemberTeleportEvent pmte = new PartyMemberTeleportEvent(sm, PartyMemberTeleportEvent.Reason.GOTO, tm);
				if(!(target.isOnline() && tm.canTp() && player.canSee(target.getPlayer())))
				{
					pmte.setCancelled(true);
				}
				inst.getServer().getPluginManager().callEvent(pmte);
				if(!pmte.isCancelled())
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
