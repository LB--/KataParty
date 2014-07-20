package com.lb_stuff.kataparty.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class CancellableKataPartyEvent extends KataPartyEvent implements Cancellable
{
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
