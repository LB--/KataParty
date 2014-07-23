package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.PartyRank;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class PartyMembersGui extends PartyGui
{
	private static final int TICKET = 0;
	private final IParty party;
	public PartyMembersGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, 6, plugin.getMessage("members-gui-title", p.getName()));
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

		final IParty.IMember mt = inst.getPartySet().findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == PartyRank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == PartyRank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		addButton(TICKET, party.getName(), Material.NAME_TAG, new ArrayList<String>(){
		{
			add(inst.getMessage("members-return"));
		}});

		int buttons = 0;
		for(final IParty.IMember m : party)
		{
			final OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
			final Player onp = offp.getPlayer();
			addButton(++buttons, new ItemStack(Material.SKULL_ITEM, (m.getRank().equals(PartyRank.MODERATOR)? 2 : (m.getRank().equals(PartyRank.ADMIN)? 3 : 1)), (short)3));
			setButton(buttons, (offp.getName() != null ? offp.getName() : m.getUuid().toString()), new ArrayList<String>(){
			{
				if(offp.getName() == null)
				{
					add(inst.getMessage("members-missing-player-file"));
				}
				if(m.getUuid().equals(player.getUniqueId()))
				{
					add(inst.getMessage("members-yourself"));
				}
				add(inst.getMessage("members-rank", m.getRankName()));
				switch(m.getRank())
				{
					case MEMBER:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							add(inst.getMessage("members-promote-moderator"));
						}
						if(isAdmin || (isMember && isPartyMod))
						{
							add(inst.getMessage("members-demote-kick"));
						}
					} break;
					case MODERATOR:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							add(inst.getMessage("members-promote-admin"));
							add(inst.getMessage("members-demote-member"));
						}
					} break;
					case ADMIN:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							add(inst.getMessage("members-demote-moderator"));
						}
					} break;
					default: break;
				}
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
		update();
		if(getButtonName(TICKET) == null)
		{
			return;
		}

		if(slot == TICKET)
		{
			new PartyManageGui(inst, player, party).show();
			return;
		}
		IParty.IMember target = party.findMember(getButtonName(slot));
		if(target == null || target.getParty() != party)
		{
			return;
		}

		final IParty.IMember mt = inst.getPartySet().findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == PartyRank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == PartyRank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		switch(click)
		{
			case LEFT:
			{
				switch(target.getRank())
				{
					case MEMBER:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(PartyRank.MODERATOR);
						}
					} break;
					case MODERATOR:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(PartyRank.ADMIN);
						}
					} break;
					default: break;
				}
			} break;
			case RIGHT:
			{
				switch(target.getRank())
				{
					case MEMBER:
					{
						if(isAdmin || (isMember && isPartyMod))
						{
							party.removeMember(target.getUuid(), PartyMemberLeaveEvent.Reason.KICKED);
						}
					} break;
					case MODERATOR:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(PartyRank.MEMBER);
						}
					} break;
					case ADMIN:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(PartyRank.MODERATOR);
						}
					} break;
					default: break;
				}
			} break;
			default: break;
		}
	}
}
