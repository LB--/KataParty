package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;

/**
 * Event called when a {@link org.bukkit.entity.Player} tries to leave an
 * {@link com.lb_stuff.kataparty.api.IParty}.
 */
public class PartyMemberLeaveEvent extends CancellableKataPartyEvent
{
	/**
	 * The reason for leaving.
	 */
	public enum Reason
	{
		/**
		 * The {@link com.lb_stuff.kataparty.api.IParty.IMember} intends
		 * to join another {@link com.lb_stuff.kataparty.api.IParty}.
		 */
		SWITCH_PARTIES,
		/**
		 * The {@link com.lb_stuff.kataparty.api.IParty.IMember} is
		 * leaving of their own accord.
		 */
		VOLUNTARY,
		/**
		 * The {@link com.lb_stuff.kataparty.api.IParty.IMember} was
		 * kicked from the {@link com.lb_stuff.kataparty.api.IParty}.
		 */
		KICKED,
		/**
		 * The {@link com.lb_stuff.kataparty.api.IParty} was
		 * {@link com.lb_stuff.kataparty.api.IParty#disband(com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason, org.bukkit.entity.Player) disbanded}.
		 */
		DISBAND,
		/**
		 */
		OTHER
	}
	private final IParty.IMember member;
	private final Reason reason;
	/**
	 * @param m The {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 * @param r The {@link Reason}.
	 */
	public PartyMemberLeaveEvent(IParty.IMember m, Reason r)
	{
		if(m == null)
		{
			throw new IllegalArgumentException("Member cannot be null");
		}
		if(r == null)
		{
			throw new IllegalArgumentException("Reason cannot be null");
		}
		member = m;
		reason = r;
	}

	/**
	 * Returns the {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 * @return The {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 */
	public IParty.IMember getMember()
	{
		return member;
	}
	/**
	 * Returns the {@link Reason}.
	 * @return The {@link Reason}.
	 */
	public Reason getReason()
	{
		return reason;
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
