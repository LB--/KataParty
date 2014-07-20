package com.lb_stuff.kataparty.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TicketInventoryEvent extends Event implements Cancellable
{
	private final Inventory inv;
	private final ItemStack ticket;
	public TicketInventoryEvent(Inventory i, ItemStack ticket_is)
	{
		inv = i;
		ticket = ticket_is;
	}

	public Inventory getInventory()
	{
		return inv;
	}
	public ItemStack getTicket()
	{
		return ticket;
	}

	private boolean cancelled = false;
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}
	@Override
	public void setCancelled(boolean c)
	{
		cancelled = c;
	}

	private static final HandlerList handlers = new HandlerList();
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
