package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.Party;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PartyMembersGui extends PartyGui
{
	private static final int RETURN = 0;
	private final Party party;
	public PartyMembersGui(KataPartyPlugin plugin, Player plr, Party p)
	{
		super(plugin, plr, 6, plugin.getMessage("members-gui-title", p.getName()));
		party = p;

		final Party.Member mt = inst.getParties().findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		addButton(RETURN, party.getName(), Material.NAME_TAG, new ArrayList<String>(){
		{
			add(inst.getMessage("members-return"));
		}});

		int buttons = 0;
		for(final Party.Member m : party)
		{
			final OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
			final Player onp = offp.getPlayer();
			addButton(++buttons, new ItemStack(Material.SKULL_ITEM, (m.getRank().equals(Party.Rank.MODERATOR)? 2 : (m.getRank().equals(Party.Rank.ADMIN)? 3 : 1)), (short)3));
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
		Party p = inst.getParties().findParty(party.getName());
		if(p == null)
		{
			hide();
			return;
		}
		if(slot == RETURN)
		{
			new PartyManageGui(inst, player, party).show();
			return;
		}
		Party.Member target = party.findMember(getButtonName(slot));
		if(target == null || target.getParty() != party)
		{
			new PartyMembersGui(inst, player, party).show();
			return;
		}

		final Party.Member mt = inst.getParties().findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == Party.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == Party.Rank.MODERATOR);
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
							target.setRank(Party.Rank.MODERATOR);
							new PartyMembersGui(inst, player, party).show();
						}
					} break;
					case MODERATOR:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(Party.Rank.ADMIN);
							new PartyMembersGui(inst, player, party).show();
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
							party.removeMember(target.getUuid());
							new PartyMembersGui(inst, player, party).show();
						}
					} break;
					case MODERATOR:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(Party.Rank.MEMBER);
							new PartyMembersGui(inst, player, party).show();
						}
					} break;
					case ADMIN:
					{
						if(isAdmin || (isMember && isPartyAdmin))
						{
							target.setRank(Party.Rank.MODERATOR);
							new PartyMembersGui(inst, player, party).show();
						}
					} break;
					default: break;
				}
			} break;
			default: break;
		}
	}
}
