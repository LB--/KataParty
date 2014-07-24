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
				setLore(new ArrayList<String>(){
				{
					add(inst.getMessage("members-return"));
				}});
				return super.display();
			}
			hide();
			return null;
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(inst.getPartySet().contains(party))
			{
				new PartyManageGui(inst, player, party).show();
				return true;
			}
			return false;
		}
	}
	private final IParty party;
	public PartyMembersGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, 6, plugin.getMessage("members-gui-title", p.getName()));
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
				switch(m.getRank())
				{
					case ADMIN:
					{
						setValue(3);
					} break;
					case MODERATOR:
					{
						setValue(2);
					} break;
					case MEMBER:
					{
						setValue(1);
					} break;
					default: break;
				}
				final OfflinePlayer offp = inst.getServer().getOfflinePlayer(m.getUuid());
				setName(offp.getName());
				final Player onp = offp.getPlayer();
				setLore(new ArrayList<String>(){
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
							if(isAdmin() || (isMember() && isPartyAdmin()))
							{
								add(inst.getMessage("members-promote-moderator"));
							}
							if(isAdmin() || (isMember() && isPartyMod()))
							{
								add(inst.getMessage("members-demote-kick"));
							}
						} break;
						case MODERATOR:
						{
							if(isAdmin() || (isMember() && isPartyAdmin()))
							{
								add(inst.getMessage("members-promote-admin"));
								add(inst.getMessage("members-demote-member"));
							}
						} break;
						case ADMIN:
						{
							if(isAdmin() || (isMember() && isPartyAdmin()))
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
				switch(click)
				{
					case LEFT:
					{
						switch(m.getRank())
						{
							case MEMBER:
							{
								if(isAdmin() || (isMember() && isPartyAdmin()))
								{
									m.setRank(PartyRank.MODERATOR);
								}
							} break;
							case MODERATOR:
							{
								if(isAdmin() || (isMember() && isPartyAdmin()))
								{
									m.setRank(PartyRank.ADMIN);
								}
							} break;
							default: break;
						}
					} break;
					case RIGHT:
					{
						switch(m.getRank())
						{
							case MEMBER:
							{
								if(isAdmin() || (isMember() && isPartyMod()))
								{
									party.removeMember(m.getUuid(), PartyMemberLeaveEvent.Reason.KICKED);
								}
							} break;
							case MODERATOR:
							{
								if(isAdmin() || (isMember() && isPartyAdmin()))
								{
									m.setRank(PartyRank.MEMBER);
								}
							} break;
							case ADMIN:
							{
								if(isAdmin() || (isMember() && isPartyAdmin()))
								{
									m.setRank(PartyRank.MODERATOR);
								}
							} break;
							default: break;
						}
					} break;
					default: break;
				}
			}
			return false;
		}
	}

	private IParty.IMember getMember()
	{
		return inst.getPartySet().findMember(player.getUniqueId());
	}
	private boolean isMember()
	{
		return getMember() != null;
	}
	private boolean isAdmin()
	{
		return player.hasPermission("KataParty.admin");
	}
	private boolean isPartyAdmin()
	{
		IParty.IMember m = getMember();
		return m != null && m.getRank() == PartyRank.ADMIN;
	}
	private boolean isPartyMod()
	{
		IParty.IMember m = getMember();
		return m != null && (m.getRank() == PartyRank.ADMIN || m.getRank() == PartyRank.MODERATOR);
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
			addButton(++buttons, new ListedMemberButton(m));
		}
	}
}
