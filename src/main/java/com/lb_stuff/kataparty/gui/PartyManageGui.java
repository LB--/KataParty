package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.PartyRank;
import com.lb_stuff.kataparty.api.Perms;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberTeleportEvent;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PartyManageGui extends PartyCreateGui
{
	protected class ManageTicketButton extends TicketButton
	{
		public ManageTicketButton()
		{
			setLore(new ArrayList<String>(){
			{
				if(isMember())
				{
					add(inst.getMessage("manage-your-rank", getMember().getRank().getName(inst)));
					add(inst.getMessage("manage-leave"));
				}
				else
				{
					add(inst.getMessage("manage-not-member"));
				}
				if(isArbiter() || Perms.rename(getMember()))
				{
					add(inst.getMessage("manage-rename"));
				}
				if(isArbiter())
				{
					add(inst.getMessage("manage-admin"));
				}
			}});
		}
		@Override
		public boolean onClick(ClickType click)
		{
			switch(click)
			{
				case LEFT:
				{
					if(isMember())
					{
						party.removeMember(player.getUniqueId(), PartyMemberLeaveEvent.Reason.VOLUNTARY);
						hide();
					}
				} break;
				case RIGHT:
				{
					if(isArbiter() || Perms.rename(getMember()))
					{
						//Bukkit didn't support opening anvil inventories when ths code was written
						PartyGui pg = new PartyRenameGui(inst, player, party);
						pg.show();
						if(player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null
						|| player.getOpenInventory().getTopInventory().getType() != InventoryType.ANVIL)
						{
							pg.onInvClose(new InventoryCloseEvent(player.getOpenInventory()));
							inst.getLogger().warning("Your CB version doesn't support opening anvil inventories");
							inst.tell(player, "That feature is currently unavailable");
						}
					}
				} break;
				default: break;
			}
			return true;
		}
	}
	protected static final int MEMBERS = 1;
	protected class ManageMembersButton extends GenericGuiButton
	{
		public ManageMembersButton()
		{
			super(new ItemStack(Material.SKULL_ITEM, party.numMembers(), (short)3));
			setName(inst.getMessage("manage-members"));
			setLore(new ArrayList<String>(){
			{
				add(inst.getMessage("manage-members-online", party.getMembersOnline().size(), party.numMembers()));
				Set<IParty.IMember> mods = party.getMembersRanked(PartyRank.MODERATOR);
				Set<IParty.IMember> onmods = new HashSet<>();
				onmods.addAll(mods);
				onmods.retainAll(party.getMembersOnline());
				add(inst.getMessage("manage-mods-online", onmods.size(), mods.size()));
				Set<IParty.IMember> admins = party.getMembersRanked(PartyRank.ADMIN);
				Set<IParty.IMember> onadmins = new HashSet<>();
				onadmins.addAll(admins);
				onadmins.retainAll(party.getMembersOnline());
				add(inst.getMessage("manage-admins-online", onadmins.size(), admins.size()));
			}});
		}
		@Override
		public boolean onClick(ClickType click)
		{
			new PartyMembersGui(inst, player, party).show();
			return true;
		}
	}
	protected class ManageTeleportsButton extends TeleportsButton
	{
		@Override
		protected boolean hasRequirements()
		{
			return super.hasRequirements() && (isArbiter() || Perms.tpToggle(getMember()));
		}
	}
	protected class ManagePvpButton extends PvpButton
	{
		@Override
		protected boolean hasRequirements()
		{
			return super.hasRequirements() && (isArbiter() || Perms.pvpToggle(getMember()));
		}
	}
	protected class ManageInventoryButton extends InventoryButton
	{
		@Override
		protected boolean hasRequirements()
		{
			return super.hasRequirements() && (isArbiter() || Perms.inventoryToggle(getMember()));
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				if(party.hasInventory())
				{
					party.disableInventory(player.getLocation());
				}
				else
				{
					party.setInventory(true);
				}
				return true;
			}
			return false;
		}
	}
	protected class ManageVisibilityButton extends VisibilityButton
	{
		@Override
		protected boolean hasRequirements()
		{
			return super.hasRequirements() && (isArbiter() || Perms.visibilityToggle(getMember()));
		}
	}
	protected static final int TICKETS = 7;
	protected class ManageTicketsButton extends GenericGuiButton
	{
		public ManageTicketsButton()
		{
			super(Material.ANVIL);
			setName(inst.getMessage("manage-generate-ticket"));
		}
		@Override
		public ItemStack display()
		{
			setLore(new ArrayList<String>(){
			{
				if(isArbiter() || (Perms.inviteCreate(player) && Perms.inviteCreate(getMember())))
				{
					add(inst.getMessage("manage-click-to-use"));
				}
				else
				{
					add(inst.getMessage("manage-cannot-use"));
				}
			}});
			return super.display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(isArbiter() || (Perms.inviteCreate(player) && Perms.inviteCreate(getMember())))
			{
				inst.getTicketManager().removeTickets(player.getInventory());
				player.getWorld().dropItem(player.getLocation(), inst.getTicketManager().generateTicket(party)).setPickupDelay(0);
				return true;
			}
			return false;
		}
	}
	protected class ManageStickyButton extends StickyButton
	{
		@Override
		protected boolean hasRequirements()
		{
			return super.hasRequirements() && (isArbiter() || Perms.stickyToggle(getMember()));
		}
	}
	protected static final int DISBAND = 9;
	protected class ManageDisbandButton extends GenericGuiButton
	{
		public ManageDisbandButton()
		{
			super(Material.TNT);
			setName(inst.getMessage(isMember()? "manage-disband" : "manage-close"));
		}
		@Override
		public ItemStack display()
		{
			setLore(new ArrayList<String>(){
			{
				if(isArbiter() || (Perms.disband(player) && Perms.disband(getMember())))
				{
					add(inst.getMessage("manage-click-to-use"));
				}
				else
				{
					add(inst.getMessage("manage-cannot-use"));
				}
			}});
			return super.display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(isArbiter() || (Perms.disband(player) && Perms.disband(getMember())))
			{
				inst.getPartySet().remove(party, PartyDisbandEvent.Reason.PARTY_ADMIN_DISBAND, player);
				hide();
				return true;
			}
			return false;
		}
	}
	protected static final int TPALL = 10;
	protected class ManageTpAllButton extends GenericGuiButton
	{
		public ManageTpAllButton()
		{
			super(Material.ENDER_PORTAL_FRAME);
			setName(inst.getMessage("manage-summon"));
		}
		@Override
		public ItemStack display()
		{
			setLore(new ArrayList<String>(){
			{
				if(isArbiter() || (Perms.tpGoto(player) && Perms.tpSummon(getMember())))
				{
					add(inst.getMessage("manage-click-to-use"));
				}
				else
				{
					add(inst.getMessage("manage-cannot-use"));
				}
			}});
			return super.display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			IParty.IMember tm = getMember();
			if(isArbiter() || (Perms.tpGoto(player) && Perms.tpSummon(getMember())))
			{
				for(IParty.IMember mem : party)
				{
					if(!mem.getUuid().equals(player.getUniqueId()))
					{
						if(tm != null)
						{
							PartyMemberTeleportEvent pmte = new PartyMemberTeleportEvent(mem, PartyMemberTeleportEvent.Reason.SUMMON, tm);
							pmte.setCancelled(!mem.canTp());
							inst.getServer().getPluginManager().callEvent(pmte);
							if(pmte.isCancelled())
							{
								continue;
							}
						}
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
				return true;
			}
			return false;
		}
	}
	protected static final int SELFTP = 11;
	protected class ManageSelfTpButton extends RequirementToggleButton
	{
		public ManageSelfTpButton()
		{
			super(Material.EYE_OF_ENDER, Material.EYE_OF_ENDER, "self-teleports");
		}
		@Override
		protected boolean hasRequirements()
		{
			return Perms.tpPref(player);
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (getMember().canTp()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				getMember().setTp(!getMember().canTp());
				return true;
			}
			return false;
		}
	}

	protected final IParty party;
	public PartyManageGui(KataPartyPlugin plugin, Player plr, IParty p)
	{
		super(plugin, plr, p, 2);
		party = p;

		rename(inst.getMessage("manage-gui-title", party.getName()));
		addButton(TICKET, new ManageTicketButton());
		addButton(MEMBERS, new ManageMembersButton());
		addButton(TELEPORTS, new ManageTeleportsButton());
		addButton(PVP, new ManagePvpButton());
		addButton(INVENTORY, new ManageInventoryButton());
		addButton(VISIBLE, new ManageVisibilityButton());
		addButton(STICKY, new ManageStickyButton());
	}

	protected IParty.IMember getMember()
	{
		return inst.getPartySet().findMember(player.getUniqueId());
	}
	protected boolean isMember()
	{
		return getMember() != null;
	}
	protected boolean isArbiter()
	{
		return Perms.arbiter(player);
	}

	private final ManageTicketsButton tickets = new ManageTicketsButton();
	private final ManageDisbandButton disband = new ManageDisbandButton();
	private final ManageTpAllButton tpall = new ManageTpAllButton();
	private final ManageSelfTpButton selftp = new ManageSelfTpButton();
	@Override
	protected void onUpdate()
	{
		if(!inst.getPartySet().contains(party))
		{
			hide();
			return;
		}

		if(party.isInviteOnly())
		{
			addButton(TICKETS, tickets);
		}
		else
		{
			removeButton(TICKETS);
		}

		if(isArbiter() || Perms.disband(getMember()))
		{
			addButton(DISBAND, disband);
		}
		else
		{
			removeButton(DISBAND);
		}

		if(isArbiter() || Perms.tpSummon(getMember()))
		{
			addButton(TPALL, tpall);
		}
		else
		{
			removeButton(TPALL);
		}

		if(isMember())
		{
			addButton(SELFTP, selftp);
		}
		else
		{
			removeButton(SELFTP);
		}
	}

	@Override
	protected void onClose()
	{
	}
}
