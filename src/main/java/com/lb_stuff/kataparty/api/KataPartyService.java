package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.PartyTicketManager;

import org.bukkit.inventory.ItemStack;

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
	 * Get the {@link Messenger}.
	 * @return The {@link Messenger}.
	 */
	public Messenger getMessenger()
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

	private PartyTicketManager getTicketManager()
	{
		return inst.getTicketManager();
	}

	/**
	 * Checks if a given item is an invitation ticket.
	 * @param is The item to check.
	 * @return <code>true</code> if the item is an invitation ticket.
	 */
	public boolean isTicket(ItemStack is)
	{
		return getTicketManager().isTicket(is);
	}
	/**
	 * Checks if an invitation ticket was thrown at the invited player.
	 * @param is The item to check.
	 * @return <code>true</code> if the item is an invitation ticket and was thrown.
	 */
	public boolean wasTicketGiven(ItemStack is)
	{
		return getTicketManager().wasTicketGiven(is);
	}

	//...
}
