package com.lb_stuff.kataparty.api.event;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TicketInventoryEvent extends CancellableKataPartyEvent
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
