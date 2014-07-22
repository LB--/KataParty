package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.KataPartyService;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.Bukkit;

/**
 * An {@link org.bukkit.event.Event} called by KataParty.
 */
public class KataPartyEvent extends Event
{
	/**
	 * See {@link org.bukkit.event.Event#Event()}
	 */
	public KataPartyEvent()
	{
	}
	/**
	 * See {@link org.bukkit.event.Event#Event(boolean)}
	 */
	public KataPartyEvent(boolean isAsync)
	{
		super(isAsync);
	}

	/**
	 * Returns the {@link com.lb_stuff.kataparty.api.KataPartyService}.
	 * @return The {@link com.lb_stuff.kataparty.api.KataPartyService}.
	 */
	public final KataPartyService getService()
	{
		return Bukkit.getServicesManager().getRegistration(KataPartyService.class).getProvider();
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
