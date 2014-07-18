package com.lb_stuff.kataparty;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import static org.bukkit.ChatColor.*;

import java.util.ArrayList;

public class PartyTicketListener implements Listener
{
	private final KataPartyPlugin inst;
	public PartyTicketListener(KataPartyPlugin plugin)
	{
		inst = plugin;

		inst.getServer().getPluginManager().registerEvents(this, inst);
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
		if(is.getType().equals(Material.NAME_TAG))
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
			im.getLore().set(0, im.getLore().get(0)+RESET);
			is.setItemMeta(im);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e)
	{
		if(isTicket(e.getCurrentItem()))
		{
			//
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent e)
	{
		if(isTicket(e.getItem()))
		{
			e.setCancelled(true);
			//
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent e)
	{
		ItemStack is = e.getItemDrop().getItemStack();
		if(isTicket(is))
		{
			if(wasTicketGiven(is))
			{
				e.getItemDrop().remove();
				Party p = getTicketParty(is);
				if(p != null)
				{
					inst.tellMessage(e.getPlayer(), "ticket-reject-inform", p.getName());
					p.informMembersMessage("ticket-reject-inform-members", e.getPlayer().getDisplayName());
				}
			}
			else
			{
				setTicketGiven(is);
			}
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onPickup(InventoryPickupItemEvent e)
	{
		ItemStack im = e.getItem().getItemStack();
		if(isTicket(im))
		{
			InventoryHolder ih = e.getInventory().getHolder();
			if(ih instanceof Player)
			{
				//
			}
			else
			{
				e.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent e)
	{
		for(ItemStack is : e.getPlayer().getInventory().getContents())
		{
			if(isTicket(is))
			{
				is.setType(Material.AIR);
				is.setAmount(0);
			}
		}
	}
}
