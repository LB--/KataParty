package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public final class PartyManageGui extends PartyGui
{
	private static final int TICKET = 0;
	private static final int MEMBERS = 1;
	private static final int TELEPORTS = 2;
	private static final int PVP = 3;
	private static final int INVENTORY = 4;
	private static final int VISIBLE = 5;
	private static final int INVITES = 6;
	private static final int TICKETS = 7;
	private static final int STICKY = 8;
	private static final int DISBAND = 9;
	private static final int TPALL = 10;
	private static final int SELFTP = 11;
	private final IParty party;
	public PartyManageGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, 2, plugin.getMessage("manage-gui-title", p.getName()));
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

		final IParty.IMember mt = inst.getParties().findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == IParty.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == IParty.Rank.MODERATOR);
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
			if(isMember)
			{
				add(inst.getMessage("manage-your-rank", mt.getRankName()));
				add(inst.getMessage("manage-leave"));
			}
			else
			{
				add(inst.getMessage("manage-not-member"));
			}
			if(isAdmin || isPartyAdmin)
			{
				add(inst.getMessage("manage-rename"));
			}
			if(isAdmin)
			{
				add(inst.getMessage("manage-admin"));
			}
		}});
		addButton(MEMBERS, new ItemStack(Material.SKULL_ITEM, party.numMembers(), (short)3));
		setButton(MEMBERS, inst.getMessage("manage-members"), new ArrayList<String>(){
		{
			add(inst.getMessage("manage-members-online", party.getMembersOnline().size(), party.numMembers()));
			Set<IParty.IMember> mods = party.getMembersRanked(IParty.Rank.MODERATOR);
			Set<IParty.IMember> onmods = new HashSet<>();
			onmods.addAll(mods);
			onmods.retainAll(party.getMembersOnline());
			add(inst.getMessage("manage-mods-online", onmods.size(), mods.size()));
			Set<IParty.IMember> admins = party.getMembersRanked(IParty.Rank.ADMIN);
			Set<IParty.IMember> onadmins = new HashSet<>();
			onadmins.addAll(admins);
			onadmins.retainAll(party.getMembersOnline());
			add(inst.getMessage("manage-admins-online", onadmins.size(), admins.size()));
		}});
		addButton(TELEPORTS, inst.getMessage(party.canTp()? "manage-teleports-enabled" : "manage-teleports-disabled"), Material.ENDER_PEARL, new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.teleport.disable") && isPartyMod))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(TELEPORTS, (party.canTp()? 2 : 1));
		addButton(PVP, inst.getMessage(party.canPvp()? "manage-pvp-enabled" : "manage-pvp-disabled"), (party.canPvp()? Material.GOLD_SWORD : Material.STONE_SWORD), new ArrayList<String>(){
		{
			if(isAdmin || isPartyMod)
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(PVP, (party.canPvp()? 2 : 1));
		addButton(INVENTORY, inst.getMessage(party.getInventory() != null? "manage-inventory-enabled" : "manage-inventory-disabled"), (party.getInventory() != null? Material.ENDER_CHEST : Material.CHEST), new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.inventory.enable") && isPartyMod))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(INVENTORY, (party.getInventory() != null? 2 : 1));
		addButton(VISIBLE, inst.getMessage(party.isVisible()? "manage-visibility-enabled" : "manage-visibility-disabled"), (party.isVisible()? Material.JACK_O_LANTERN : Material.PUMPKIN), new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.hide") && isPartyAdmin))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(VISIBLE, (party.isVisible()? 2 : 1));
		addButton(INVITES, inst.getMessage(party.isInviteOnly()? "manage-invites-enabled" : "manage-invites-disabled"), (party.isInviteOnly()? Material.IRON_DOOR : Material.WOOD_DOOR), new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.invite.enforce") && isPartyAdmin))
			{
				add(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-change"));
			}
		}});
		setButton(INVITES, (party.isInviteOnly()? 2 : 1));
		if(party.isInviteOnly())
		{
			addButton(TICKETS, inst.getMessage("manage-generate-ticket"), Material.ANVIL, new ArrayList<String>(){
			{
				if(isAdmin || (player.hasPermission("KataParty.invite.create") && isPartyAdmin))
				{
					add(inst.getMessage("manage-click-to-use"));
				}
				else
				{
					add(inst.getMessage("manage-cannot-use"));
				}
			}});
		}
		if(!inst.getParties().keepEmptyParties() && player.hasPermission("KataParty.stick"))
		{
			addButton(STICKY, inst.getMessage(party.isSticky()? "manage-sticky-enabled" : "manage-sticky-disabled"), Material.STICK, new ArrayList<String>(){
			{
				add(inst.getMessage("manage-click-to-change"));
			}});
			setButton(STICKY, (party.isSticky()? 2 : 1));
		}
		addButton(DISBAND, inst.getMessage(isMember? "manage-disband" : "manage-close"), Material.TNT, new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.disband") && isPartyAdmin))
			{
				add(inst.getMessage("manage-click-to-use"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-use"));
			}
		}});
		addButton(TPALL, inst.getMessage("manage-summon"), Material.ENDER_PORTAL_FRAME, new ArrayList<String>(){
		{
			if(isAdmin || (player.hasPermission("KataParty.teleport.do") && isPartyAdmin))
			{
				add(inst.getMessage("manage-click-to-use"));
			}
			else
			{
				add(inst.getMessage("manage-cannot-use"));
			}
		}});
		if(isMember)
		{
			addButton(SELFTP, inst.getMessage(mt.canTp()? "manage-self-teleports-enabled" : "manage-self-teleports-disabled"), Material.EYE_OF_ENDER, new ArrayList<String>(){
			{
				if(player.hasPermission("KataParty.teleport.disallow"))
				{
					add(inst.getMessage("manage-click-to-change"));
				}
				else
				{
					add(inst.getMessage("manage-cannot-change"));
				}
			}});
			setButton(SELFTP, (mt.canTp()? 2 : 1));
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

		final IParty.IMember mt = inst.getParties().findMember(player.getUniqueId());
		boolean is_member = false;
		boolean is_admin = false;
		boolean is_partyAdmin = false;
		boolean is_partyMod = false;
		if(mt != null && mt.getParty() == party)
		{
			is_member = true;
			is_partyAdmin = (mt.getRank() == IParty.Rank.ADMIN);
			is_partyMod = (is_partyAdmin || mt.getRank() == IParty.Rank.MODERATOR);
		}
		if(player.hasPermission("KataParty.admin"))
		{
			is_admin = true;
		}
		final boolean isMember = is_member;
		final boolean isAdmin = is_admin;
		final boolean isPartyAdmin = is_partyAdmin;
		final boolean isPartyMod = is_partyMod;

		switch(slot)
		{
			case TICKET:
			{
				switch(click)
				{
					case LEFT:
					{
						if(isMember)
						{
							party.removeMember(player.getUniqueId());
							hide();
						}
					} break;
					case RIGHT:
					{
						if(isAdmin || isPartyAdmin)
						{
							//Bukkit didn't support opening anvil inventories when ths code was written
							PartyGui pg = new PartyRenameGui(inst, player, party);
							pg.show();
							if(player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null
							|| player.getOpenInventory().getTopInventory().getType() != InventoryType.ANVIL)
							{
								pg.onInvClose(new InventoryCloseEvent(player.getOpenInventory()));
								inst.getLogger().warning("Your CB version doesn't support opening anvil inventories");
								inst.tell(player, "That feature is currently disabled");
							}
						}
					} break;
					default: break;
				}
			} break;
			case MEMBERS:
			{
				new PartyMembersGui(inst, player, party).show();
			} break;
			case TELEPORTS:
			{
				if(isAdmin || (player.hasPermission("KataParty.teleport.disable") && isPartyMod))
				{
					party.setTp(!party.canTp());
				}
			} break;
			case PVP:
			{
				if(isAdmin || isPartyMod)
				{
					party.setPvp(!party.canPvp());
				}
			} break;
			case INVENTORY:
			{
				if(isAdmin || (player.hasPermission("KataParty.inventory.enable") && isPartyMod))
				{
					if(party.getInventory() == null)
					{
						party.enableInventory();
					}
					else
					{
						party.disableInventory(player.getEyeLocation());
					}
				}
			} break;
			case VISIBLE:
			{
				if(isAdmin || (player.hasPermission("KataParty.hide") && isPartyAdmin))
				{
					party.setVisible(!party.isVisible());
				}
			} break;
			case INVITES:
			{
				if(isAdmin || (player.hasPermission("KataParty.invite.enforce") && isPartyAdmin))
				{
					party.setInviteOnly(!party.isInviteOnly());
				}
			} break;
			case TICKETS:
			{
				if(isAdmin || (player.hasPermission("KataParty.invite.create") && isPartyAdmin))
				{
					inst.getTicketManager().removeTickets(player.getInventory());
					player.getWorld().dropItem(player.getLocation(), inst.getTicketManager().generateTicket(party)).setPickupDelay(0);
				}
			} break;
			case STICKY:
			{
				if(!inst.getParties().keepEmptyParties() && player.hasPermission("KataParty.stick"))
				{
					party.setSticky(!party.isSticky());
				}
			} break;
			case DISBAND:
			{
				if(isAdmin || (player.hasPermission("KataParty.disband") && isPartyAdmin))
				{
					inst.getParties().remove(party, PartyDisbandEvent.Reason.PARTY_ADMIN_DISBAND, player);
					hide();
				}
			} break;
			case TPALL:
			{
				if(isAdmin || (player.hasPermission("KataParty.teleport.do") && isPartyAdmin))
				{
					for(IParty.IMember mem : party)
					{
						if(!mem.getUuid().equals(player.getUniqueId()) && mem.canTp())
						{
							OfflinePlayer offp = inst.getServer().getOfflinePlayer(mem.getUuid());
							if(offp.isOnline())
							{
								Player onp = offp.getPlayer();
								onp.setNoDamageTicks(20*5); //inulnerable for 5 seconds
								onp.teleport(player);
								inst.tellMessage(onp, "member-teleported-to", player.getDisplayName());
							}
						}
					}
					hide();
				}
			} break;
			case SELFTP:
			{
				if(isMember && player.hasPermission("KataParty.teleport.disallow"))
				{
					mt.setTp(!mt.canTp());
				}
			} break;
			default: break;
		}
	}
}
