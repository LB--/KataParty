package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IPartySettings;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Event called when a KataParty is created.
 */
public class PartyCreateEvent extends CancellableKataPartyEvent
{
	private final Player player;
	private final IPartySettings settings;
	/**
	 * @param creator The {@link org.bukkit.entity.Player} that
	 * created the party. May be null.
	 * @param sett The settings the party will be created with.
	 */
	public PartyCreateEvent(Player creator, IPartySettings sett)
	{
		if(sett == null)
		{
			throw new IllegalArgumentException("Party settings cannot be null");
		}
		player = creator;
		settings = sett;
	}

	/**
	 * Returns the creator of the party, if there is one.
	 * @return The {@link org.bukkit.entity.Player}. May be null.
	 */
	public Player getCreator()
	{
		return player;
	}
	/**
	 * Gets the settings the party will be created with. These
	 * settings can be modified through the returned object.
	 * @return The settings the party will be created with.
	 */
	public IPartySettings getSettings()
	{
		return settings;
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
