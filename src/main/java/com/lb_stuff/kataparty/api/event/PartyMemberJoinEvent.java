package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;

import org.bukkit.event.HandlerList;

/**
 * Event called when a {@link org.bukkit.entity.Player} tries to join an
 * {@link com.lb_stuff.kataparty.api.IParty}.
 */
public final class PartyMemberJoinEvent extends CancellableKataPartyEvent
{
	/**
	 * The reason for joining.
	 */
	public enum Reason
	{
		/**
		 * The applicant is the {@link org.bukkit.entity.Player} who created
		 * the {@link com.lb_stuff.kataparty.api.IParty}.
		 */
		CREATOR,
		/**
		 * The applicant is joining voluntarily.
		 */
		VOLUNTARY,
		/**
		 * The was invited and used their
		 * {@link com.lb_stuff.kataparty.api.IPartyTicketManager invitation ticket}
		 * to join.
		 */
		INVITATION,
		/**
		 */
		OTHER
	}
	private final IParty party;
	private final IMemberSettings settings;
	private final Reason reason;
	/**
	 * @param p The {@link com.lb_stuff.kataparty.api.IParty} being joined.
	 * @param applicant The {@link com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings}.
	 * @param r The {@link Reason} for application.
	 */
	public PartyMemberJoinEvent(IParty p, IMemberSettings applicant, Reason r)
	{
		if(p == null)
		{
			throw new IllegalArgumentException("Party cannot be null");
		}
		if(applicant == null)
		{
			throw new IllegalArgumentException("Applicant cannot be null");
		}
		else if(p.getPartySet().findMember(applicant.getUuid()) != null)
		{
			throw new IllegalArgumentException("Applicant cannot be in a party");
		}
		if(r == null)
		{
			throw new IllegalArgumentException("Reason cannot be null");
		}
		party = p;
		settings = applicant;
		reason = r;
	}

	/**
	 * Returns the {@link com.lb_stuff.kataparty.api.IParty} being joined.
	 * @return The {@link com.lb_stuff.kataparty.api.IParty} being joined.
	 */
	public IParty getParty()
	{
		return party;
	}
	/**
	 * Returns the applicant's {@link com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings}.
	 * @return The applicant's {@link com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings}.
	 */
	public IMemberSettings getApplicant()
	{
		return settings;
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
