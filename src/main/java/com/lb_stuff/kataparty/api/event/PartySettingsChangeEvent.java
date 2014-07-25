package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Event called when an {@link com.lb_stuff.kataparty.api.IParty}
 * is having one of its {@link com.lb_stuff.kataparty.api.IPartySettings}
 * changed.
 */
public final class PartySettingsChangeEvent extends CancellableKataPartyEvent
{
	private final IParty party;
	private final IPartySettings change;
	/**
	 * @param p The {@link com.lb_stuff.kataparty.api.IParty}.
	 * @param s The new {@link com.lb_stuff.kataparty.api.IPartySettings}.
	 */
	public PartySettingsChangeEvent(IParty p, IPartySettings s)
	{
		if(p == null)
		{
			throw new IllegalArgumentException("Party cannot be null");
		}
		if(s == null)
		{
			throw new IllegalArgumentException("Settings cannot be null");
		}
		party = p;
		change = s;
	}

	/**
	 * Returns the {@link com.lb_stuff.kataparty.api.IParty} being changed.
	 * @return The {@link com.lb_stuff.kataparty.api.IParty} being changed.
	 */
	public IParty getParty()
	{
		return party;
	}
	/**
	 * Returns the new {@link com.lb_stuff.kataparty.api.IPartySettings}.
	 * Changing the returned value has no effect on the result of this event.
	 * @return the new {@link com.lb_stuff.kataparty.api.IPartySettings}.
	 */
	public IPartySettings getChanges()
	{
		return change;
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
