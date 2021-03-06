package com.lb_stuff.kataparty.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public abstract class CancellableKataPartyEvent extends KataPartyEvent implements Cancellable
{
	/**
	 * See {@link org.bukkit.event.Event#Event()}
	 */
	public CancellableKataPartyEvent()
	{
	}
	/**
	 * See {@link org.bukkit.event.Event#Event(boolean)}
	 * @param isAsync See {@link org.bukkit.event.Event#Event(boolean)}
	 */
	public CancellableKataPartyEvent(boolean isAsync)
	{
		super(isAsync);
	}

	private boolean cancelled = false;
	/**
	 * See {@link org.bukkit.event.Cancellable#isCancelled()}
	 * @return See {@link org.bukkit.event.Cancellable#isCancelled()}
	 */
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}
	/**
	 * See {@link org.bukkit.event.Cancellable#setCancelled(boolean)}
	 * @param c See {@link org.bukkit.event.Cancellable#setCancelled(boolean)}
	 */
	@Override
	public void setCancelled(boolean c)
	{
		cancelled = c;
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
