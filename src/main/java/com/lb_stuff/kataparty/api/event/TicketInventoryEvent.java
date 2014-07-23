package com.lb_stuff.kataparty.api.event;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryAction;

/**
 * Event called when a {@link org.bukkit.entity.Player} tries to place or remove an
 * {@link com.lb_stuff.kataparty.api.IPartyTicketManager invitation ticket}. This
 * event generally comes already cancelled.
 */
public final class TicketInventoryEvent extends CancellableKataPartyEvent
{
	private final Inventory inv;
	private final ItemStack ticket;
	private final InventoryAction action;
	/**
	 * @param i The {@link org.bukkit.inventory.Inventory}.
	 * @param ticket_is The {@link com.lb_stuff.kataparty.api.IPartyTicketManager invitation ticket}.
	 * @param a The {@link org.bukkit.event.inventory.InventoryAction}.
	 */
	public TicketInventoryEvent(Inventory i, ItemStack ticket_is, InventoryAction a)
	{
		if(i == null)
		{
			throw new IllegalArgumentException("Inventory cannot be null");
		}
		if(ticket_is == null)
		{
			throw new IllegalArgumentException("Ticket cannot be null");
		}
		if(a == null)
		{
			throw new IllegalArgumentException("Action cannot be null");
		}
		inv = i;
		ticket = ticket_is;
		action = a;
	}

	/**
	 * Returns the {@link org.bukkit.inventory.Inventory}.
	 * @return The {@link org.bukkit.inventory.Inventory}.
	 */
	public Inventory getInventory()
	{
		return inv;
	}
	/**
	 * Returns the {@link com.lb_stuff.kataparty.api.IPartyTicketManager invitation ticket}.
	 * @return The {@link com.lb_stuff.kataparty.api.IPartyTicketManager invitation ticket}.
	 */
	public ItemStack getTicket()
	{
		return ticket;
	}
	/**
	 * Returns the {@link org.bukkit.event.inventory.InventoryAction}.
	 * @return The {@link org.bukkit.event.inventory.InventoryAction}.
	 */
	public InventoryAction getAction()
	{
		return action;
	}

	private static final HandlerList handlers = new HandlerList();
	/**
	 * See {@link org.bukkit.event.Event#getHandlers()}.
	 * @return The {@link org.bukkit.event.HandlerList}.
	 */
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	/**
	 * See {@link org.bukkit.event.Event#getHandlers()}.
	 * @return The {@link org.bukkit.event.HandlerList}.
	 */
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
