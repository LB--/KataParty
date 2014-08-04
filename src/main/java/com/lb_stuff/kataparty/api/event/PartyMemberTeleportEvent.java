package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.event.HandlerList;

/**
 * Event called when a {@link org.bukkit.entity.Player} tries to teleport to
 * another {@link org.bukkit.entity.Player} in their
 * {@link com.lb_stuff.kataparty.api.IParty}.
 */
public final class PartyMemberTeleportEvent extends CancellableKataPartyEvent
{
	/**
	 * The reason for teleporting.
	 */
	public enum Reason
	{
		/**
		 * The teleport was initiated by the source.
		 */
		GOTO,
		/**
		 * The teleport was initiated by the target.
		 */
		SUMMON,
		/**
		 */
		OTHER
	}
	private final IParty.IMember source;
	private final Reason reason;
	private final IParty.IMember target;
	/**
	 * @param s The source {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 * @param r The {@link Reason}.
	 * @param t The target {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 */
	public PartyMemberTeleportEvent(IParty.IMember s, Reason r, IParty.IMember t)
	{
		if(s == null)
		{
			throw new IllegalArgumentException("Source cannot be null");
		}
		if(r == null)
		{
			throw new IllegalArgumentException("Reason cannot be null");
		}
		if(t == null)
		{
			throw new IllegalArgumentException("Target cannot be null");
		}
		if(s.getParty() != t.getParty())
		{
			throw new IllegalArgumentException("Source and Target must be in same party");
		}
		source = s;
		reason = r;
		target = t;
	}

	/**
	 * Returns the source {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 * @return The source {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 */
	public IParty.IMember getSource()
	{
		return source;
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
	 * Returns the target {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 * @return The target {@link com.lb_stuff.kataparty.api.IParty.IMember}.
	 */
	public IParty.IMember getTarget()
	{
		return target;
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
