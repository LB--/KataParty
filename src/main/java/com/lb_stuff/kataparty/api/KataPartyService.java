package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.KataPartyPlugin;

/**
 * The main service for the KataParty API.
 */
public class KataPartyService
{
	private final KataPartyPlugin inst;
	/**
	 * Constructs the service (implementation use only).
	 * @param plugin The instance of KataParty.
	 */
	public KataPartyService(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	/**
	 * Get the {@link IMessenger}.
	 * @return The {@link IMessenger}.
	 */
	public IMessenger getMessenger()
	{
		return inst;
	}
	/**
	 * Get the set of parties.
	 * @return The {@link IPartySet} instance.
	 */
	public IPartySet getPartySet()
	{
		return inst.getParties();
	}
	/**
	 * Get the manager for invitation tickets.
	 * @return The {@link ITicketManager} instance.
	 */
	public IPartyTicketManager getTicketManager()
	{
		return inst.getTicketManager();
	}

	//...
}
