package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Event called when an {@link com.lb_stuff.kataparty.api.IParty}
 * is being {@link com.lb_stuff.kataparty.api.IParty#disband(com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason, org.bukkit.entity.Player) disbanded}.
 */
public final class PartyDisbandEvent extends CancellableKataPartyEvent
{
	/**
	 * The reason for which a party is being disbanded.
	 */
	public enum Reason
	{
		/**
		 * A member with {@link com.lb_stuff.kataparty.api.IParty.Rank#ADMIN}
		 * chose to disband the party.
		 */
		PARTY_ADMIN_DISBAND,
		/**
		 * A server administrator forced the party to close.
		 */
		SERVER_ADMIN_CLOSE,
		/**
		 * The party was automatically closed for being empty.
		 */
		AUTOMATIC_CLOSE,
		/**
		 */
		OTHER
	}
	private final IParty party;
	private final Reason reason;
	private final Player player;
	/**
	 * @param p The {@link com.lb_stuff.kataparty.api.IParty} being closed.
	 * @param r The {@link Reason} the party is being closed.
	 * @param plr The {@link org.bukkit.entity.Player} that closed the party,
	 * if any. May be null.
	 */
	public PartyDisbandEvent(IParty p, Reason r, Player plr)
	{
		if(p == null)
		{
			throw new IllegalArgumentException("Party cannot be null");
		}
		if(r == null)
		{
			throw new IllegalArgumentException("Reason cannot be null");
		}
		party = p;
		reason = r;
		player = plr;
	}

	/**
	 * Returns the {@link com.lb_stuff.kataparty.api.IParty} being closed.
	 * @return The {@link com.lb_stuff.kataparty.api.IParty} being closed.
	 */
	public IParty getParty()
	{
		return party;
	}
	/**
	 * Returns the {@link Reason} for closing the party.
	 * @return The {@link Reason} for closing the party.
	 */
	public Reason getReason()
	{
		return reason;
	}
	/**
	 * Returns the {@link org.bukkit.entity.Player} that closed the party,
	 * if any. May return null.
	 * @return The {@link org.bukkit.entity.Player} that closed the party,
	 * if any. May by null.
	 */
	public Player getCloser()
	{
		return player;
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
