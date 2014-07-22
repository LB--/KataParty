package com.lb_stuff.kataparty.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Implementing classes are responsible for managing invitation tickets to parties.
 * This interface should only be implemented by KataParty.
 */
public interface IPartyTicketManager
{
	/**
	 * Generate an invitation ticket to an {@link IParty}.
	 * @param p The {@link IParty} for which the invitation ticket should be generated.
	 * @return The invitation ticket as an {@link org.bukkit.inventory.ItemStack}.
	 */
	ItemStack generateTicket(final IParty p);
	/**
	 * Get the {@link IParty} the given invitation ticket is for.
	 * @param is A valid invitation ticket.
	 * @return The {@link IParty} the given invitation ticket is for.
	 */
	IParty getTicketParty(ItemStack is);
	/**
	 * Checks if a given item is an invitation ticket.
	 * @param is The item to check.
	 * @return <code>true</code> if the item is an invitation ticket.
	 */
	boolean isTicket(ItemStack is);
	/**
	 * Removes all invitation tickets from the given {@link org.bukkit.inventory.Inventory}.
	 * @param inv The {@link org.bukkit.inventory.Inventory} from which to remove
	 * invitation tickets.
	 */
	void removeTickets(Inventory inv);
	/**
	 * Modify an invitation ticket so that its help text is oriented toward
	 * invited players instead of the player who created the ticket.
	 * @param is A valid invitation ticket.
	 */
	void setTicketGiven(ItemStack is);
	/**
	 * Checks if an invitation ticket was thrown at the invited player.
	 * @param is The item to check.
	 * @return <code>true</code> if the item is an invitation ticket and was thrown.
	 */
	boolean wasTicketGiven(ItemStack is);
}
