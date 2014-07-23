package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;

/**
 * Event called when a {@link org.bukkit.entity.Player} tries to leave an
 * {@link com.lb_stuff.kataparty.api.IParty}.
 */
public final class PartyMemberLeaveEvent extends CancellableKataPartyEvent
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
		 * {@link com.lb_stuff.kataparty.api.IParty#disband(com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason, org.bukkit.entity.Player) disbanded},
		 * and the event cannot be cancelled.
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

	/**
	 * Cancels or uncancels the event, unless {@link #getReason()}
	 * returns {@link Reason#DISBAND}, in which case the event
	 * cannot be cancelled.
	 * @param c Whether to cancel the event.
	 */
	@Override
	public void setCancelled(boolean c)
	{
		if(getReason() == Reason.DISBAND)
		{
			super.setCancelled(false);
			return;
		}
		super.setCancelled(c);
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
