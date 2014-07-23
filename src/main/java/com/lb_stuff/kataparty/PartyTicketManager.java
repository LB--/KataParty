package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.IPartyTicketManager;
import com.lb_stuff.kataparty.api.event.TicketInventoryEvent;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import static org.bukkit.ChatColor.*;

import java.util.List;
import java.util.ArrayList;

public class PartyTicketManager implements IPartyTicketManager, Listener
{
	private final KataPartyPlugin inst;
	public PartyTicketManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private final String PREFIX = ""+RESET+"KataParty: "+AQUA;
	@Override
	public ItemStack generateTicket(final IParty p)
	{
		ItemStack is = new ItemStack(Material.NAME_TAG);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(inst.getMessage("ticket-name"));
		im.setLore(new ArrayList<String>(){
		{
			add(PREFIX+p.getName());
			add(inst.getMessage("ticket-giver-instructions-1"));
			add(inst.getMessage("ticket-giver-instructions-2"));
		}});
		is.setItemMeta(im);
		return is;
	}
	@Override
	public boolean isTicket(ItemStack is)
	{
		if(is != null && is.getType() != null && is.getType().equals(Material.NAME_TAG) && is.hasItemMeta())
		{
			ItemMeta im = is.getItemMeta();
			if(im.hasDisplayName() && im.hasLore() && im.getLore().size() == 3)
			{
				if(im.getLore().get(0).startsWith(PREFIX))
				{
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public IParty getTicketParty(ItemStack is)
	{
		if(isTicket(is))
		{
			String pname = stripColor(is.getItemMeta().getLore().get(0).substring(PREFIX.length()));
			return inst.getPartySet().findParty(pname);
		}
		return null;
	}
	@Override
	public void removeTickets(Inventory inv)
	{
		for(int i = 0; i < inv.getSize(); ++i)
		{
			ItemStack is = inv.getItem(i);
			if(isTicket(is))
			{
				inv.setItem(i, null);
			}
		}
	}
	@Override
	public boolean wasTicketGiven(ItemStack is)
	{
		if(!isTicket(is))
		{
			throw new IllegalArgumentException();
		}
		String pname = is.getItemMeta().getLore().get(0).substring(PREFIX.length());
		return pname.endsWith(""+RESET);
	}
	@Override
	public void setTicketGiven(ItemStack is)
	{
		if(!wasTicketGiven(is))
		{
			ItemMeta im = is.getItemMeta();
			List<String> lore = im.getLore();
			lore.set(0, im.getLore().get(0)+RESET);
			lore.set(1, inst.getMessage("ticket-receiver-instructions-1"));
			lore.set(2, inst.getMessage("ticket-receiver-instructions-2"));
			im.setLore(lore);
			is.setItemMeta(im);
		}
	}


	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e)
	{
		Inventory top = e.getView().getTopInventory();
		if(top != null)
		{
			TicketInventoryEvent tie = null;
			if(isTicket(e.getCursor()))
			{
				tie = new TicketInventoryEvent(top, e.getCursor(), e.getAction());
				if(e.getRawSlot() < top.getSize())
				{
					e.setCancelled(true);
				}
			}
			if(isTicket(e.getCurrentItem()))
			{
				tie = new TicketInventoryEvent(top, e.getCurrentItem(), e.getAction());
				if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
				{
					e.setCancelled(true);
				}
			}
			if(tie != null)
			{
				tie.setCancelled(e.isCancelled());
				inst.getServer().getPluginManager().callEvent(tie);
				e.setCancelled(tie.isCancelled());
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent e)
	{
		if(isTicket(e.getPlayer().getItemInHand()))
		{
			e.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onInteract(final PlayerInteractEvent e)
	{
		final ItemStack is = e.getItem();
		if(isTicket(is))
		{
			e.setCancelled(true);
			if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				final Player player = e.getPlayer();
				final IParty p = getTicketParty(is);
				if(p != null && wasTicketGiven(is))
				{
					IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
					if(m == null || m.getParty() != p)
					{
						if(p.newMember(new MemberSettings(player.getUniqueId()), PartyMemberJoinEvent.Reason.INVITATION) != null)
						{
							inst.tellMessage(player, "ticket-accept-inform", p.getName());
							inst.getFilter().tellFilterPref(player);
						}
					}
				}
				inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
				{
					removeTickets(player.getInventory());
				}});
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDrop(final PlayerDropItemEvent e)
	{
		final Item i = e.getItemDrop();
		final ItemStack is = i.getItemStack();
		if(isTicket(is))
		{
			if(wasTicketGiven(is))
			{
				e.getItemDrop().remove();
				IParty p = getTicketParty(is);
				IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
				if(p != null && (m == null || m.getParty() != p))
				{
					inst.tellMessage(e.getPlayer(), "ticket-reject-inform", p.getName());
					p.informMembersMessage("ticket-reject-inform-members", e.getPlayer().getDisplayName());
				}
			}
			else
			{
				e.setCancelled(true);
				inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
				{
					ItemStack is2 = new ItemStack(is);
					setTicketGiven(is2);
					i.getWorld().dropItem(i.getLocation(), is2).setVelocity(i.getVelocity());
					removeTickets(e.getPlayer().getInventory());
				}});
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPickup(InventoryPickupItemEvent e)
	{
		if(isTicket(e.getItem().getItemStack()))
		{
			e.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPickup(PlayerPickupItemEvent e)
	{
		ItemStack is = e.getItem().getItemStack();
		if(isTicket(is) && wasTicketGiven(is))
		{
			Player p = e.getPlayer();
			if(p.hasPermission("KataParty.invite.accept"))
			{
				inst.tellMessage(p, "ticket-receive-inform", getTicketParty(is).getName());
			}
			else
			{
				e.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent e)
	{
		removeTickets(e.getPlayer().getInventory());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onKick(PlayerKickEvent e)
	{
		removeTickets(e.getPlayer().getInventory());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e)
	{
		removeTickets(e.getPlayer().getInventory());
	}
}
