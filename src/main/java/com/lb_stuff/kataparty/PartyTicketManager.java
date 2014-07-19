package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.TicketHolder;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
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

public class PartyTicketManager implements Listener
{
	private final KataPartyPlugin inst;
	public PartyTicketManager(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private final String PREFIX = ""+RESET+"KataParty: "+AQUA;
	public ItemStack generateTicket(final Party p)
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
	public Party getTicketParty(ItemStack is)
	{
		if(isTicket(is))
		{
			String pname = stripColor(is.getItemMeta().getLore().get(0).substring(PREFIX.length()));
			return inst.getParties().findParty(pname);
		}
		return null;
	}
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
	public boolean wasTicketGiven(ItemStack is)
	{
		if(!isTicket(is))
		{
			throw new IllegalArgumentException();
		}
		String pname = is.getItemMeta().getLore().get(0).substring(PREFIX.length());
		return pname.endsWith(""+RESET);
	}
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


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e)
	{
		Inventory top = e.getView().getTopInventory();
		if(top != null && !(top instanceof TicketHolder))
		{
			if((isTicket(e.getCursor()) && e.getRawSlot() < top.getSize())
			|| (isTicket(e.getCurrentItem()) && e.getAction().equals(e.getAction().MOVE_TO_OTHER_INVENTORY)))
			{
				e.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent e)
	{
		if(isTicket(e.getPlayer().getItemInHand()))
		{
			e.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteract(final PlayerInteractEvent e)
	{
		final ItemStack is = e.getItem();
		if(isTicket(is))
		{
			e.setCancelled(true);
			if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				final Player player = e.getPlayer();
				final Party p = getTicketParty(is);
				if(p != null && wasTicketGiven(is))
				{
					Party.Member m = inst.getParties().findMember(player.getUniqueId());
					if(m == null || m.getParty() != p)
					{
						inst.tellMessage(player, "ticket-accept-inform", p.getName());
						p.addMember(player.getUniqueId());
					}
				}
				inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
				{
					removeTickets(player.getInventory());
				}});
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDrop(final PlayerDropItemEvent e)
	{
		final Item i = e.getItemDrop();
		final ItemStack is = i.getItemStack();
		if(isTicket(is))
		{
			if(wasTicketGiven(is))
			{
				e.getItemDrop().remove();
				Party p = getTicketParty(is);
				Party.Member m = inst.getParties().findMember(e.getPlayer().getUniqueId());
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
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPickup(InventoryPickupItemEvent e)
	{
		if(isTicket(e.getItem().getItemStack()))
		{
			e.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
