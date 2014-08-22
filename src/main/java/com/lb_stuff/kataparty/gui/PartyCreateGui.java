package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IGuiButton;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.Perms;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyCreateGui extends PartyGui
{
	protected static final int TICKET = 0;
	protected class TicketButton extends GenericGuiButton
	{
		public TicketButton()
		{
			super(s.getName(), Material.NAME_TAG, inst.getMessage("create-create"), inst.getMessage("create-cancel"));
		}
		@Override
		public boolean onClick(ClickType click)
		{
			inst.getPartySet().newParty(player, s);
			hide();
			return true;
		}
	}

	protected abstract class RequirementToggleButton implements IGuiButton
	{
		protected final GenericGuiButton on;
		protected final GenericGuiButton off;
		public RequirementToggleButton(Material onmat, Material offmat, String msgname)
		{
			on = new GenericGuiButton(onmat);
			off = new GenericGuiButton(offmat);
			on.setName(inst.getMessage("manage-"+msgname+"-enabled"));
			off.setName(inst.getMessage("manage-"+msgname+"-disabled"));
			on.setValue(2);
		}
		protected abstract boolean hasRequirements();
		@Override
		public ItemStack display()
		{
			if(hasRequirements())
			{
				on.setLore(inst.getMessage("manage-click-to-change"));
				off.setLore(inst.getMessage("manage-click-to-change"));
			}
			else
			{
				on.setLore(inst.getMessage("manage-cannot-change"));
				off.setLore(inst.getMessage("manage-cannot-change"));
			}
			return null;
		}
		@Override
		public boolean onClick(ClickType click)
		{
			return hasRequirements();
		}
	}
	protected static final int TELEPORTS = 2;
	protected class TeleportsButton extends RequirementToggleButton
	{
		public TeleportsButton()
		{
			super(Material.ENDER_PEARL, Material.ENDER_PEARL, "teleports");
		}
		@Override
		protected boolean hasRequirements()
		{
			return Perms.tpToggle(player);
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (s.canTp()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				s.setTp(!s.canTp());
				return true;
			}
			return false;
		}
	}
	protected static final int PVP = 3;
	protected class PvpButton extends RequirementToggleButton
	{
		public PvpButton()
		{
			super(Material.GOLD_SWORD, Material.STONE_SWORD, "pvp");
		}
		@Override
		protected boolean hasRequirements()
		{
			return true;
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (s.canPvp()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				s.setPvp(!s.canPvp());
				return true;
			}
			return false;
		}
	}
	protected static final int INVENTORY = 4;
	protected class InventoryButton extends RequirementToggleButton
	{
		public InventoryButton()
		{
			super(Material.ENDER_CHEST, Material.CHEST, "inventory");
		}
		@Override
		protected boolean hasRequirements()
		{
			return Perms.inventoryToggle(player);
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (s.hasInventory()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				s.setInventory(!s.hasInventory());
				return true;
			}
			return false;
		}
	}
	protected static final int VISIBLE = 5;
	protected class VisibilityButton extends RequirementToggleButton
	{
		public VisibilityButton()
		{
			super(Material.JACK_O_LANTERN, Material.PUMPKIN, "visibility");
		}
		@Override
		protected boolean hasRequirements()
		{
			return Perms.visibilityToggle(player);
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (s.isVisible()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				s.setVisible(!s.isVisible());
				return true;
			}
			return false;
		}
	}
	protected static final int INVITES = 6;
	protected class InvitesButton extends RequirementToggleButton
	{
		public InvitesButton()
		{
			super(Material.IRON_DOOR, Material.WOOD_DOOR, "invites");
		}
		@Override
		protected boolean hasRequirements()
		{
			return Perms.inviteToggle(player);
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (s.isInviteOnly()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				s.setInviteOnly(!s.isInviteOnly());
				return true;
			}
			return false;
		}
	}
	protected static final int STICKY = 8;
	protected class StickyButton extends RequirementToggleButton
	{
		public StickyButton()
		{
			super(Material.STICK, Material.STICK, "sticky");
		}
		@Override
		protected boolean hasRequirements()
		{
			return Perms.stickyToggle(player);
		}
		@Override
		public ItemStack display()
		{
			super.display();
			return (s.isSticky()? on : off).display();
		}
		@Override
		public boolean onClick(ClickType click)
		{
			if(super.onClick(click))
			{
				s.setSticky(!s.isSticky());
				return true;
			}
			return false;
		}
	}

	private final IPartySettings s;
	public PartyCreateGui(KataPartyPlugin plugin, Player p, IPartySettings settings)
	{
		this(plugin, p, settings, 1);
	}
	public PartyCreateGui(KataPartyPlugin plugin, Player p, IPartySettings settings, int guirows)
	{
		super(plugin, p, guirows, plugin.getMessage("create-gui-title", settings.getName()));
		s = settings;

		addButton(TICKET, new TicketButton());
		addButton(TELEPORTS, new TeleportsButton());
		addButton(PVP, new PvpButton());
		addButton(INVENTORY, new InventoryButton());
		addButton(VISIBLE, new VisibilityButton());
		addButton(INVITES, new InvitesButton());
		addButton(STICKY, new StickyButton());
	}

	@Override
	protected void onUpdate()
	{
		if(!getButtons().isEmpty() && inst.getPartySet().findParty(s.getName()) != null)
		{
			clearButtons();
			rename(inst.getMessage("create-name-taken", s.getName()));
		}
	}

	@Override
	protected void onClose()
	{
		if(!getButtons().isEmpty())
		{
			IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
			if(m == null || !m.getParty().getName().equals(s.getName()))
			{
				inst.tellMessage(player, "create-cancelled");
			}
		}
	}
}
