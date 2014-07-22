package com.lb_stuff.kataparty.api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Holds settings for an {@link IParty}.
 * This interface may be implemented by plugins other than KataParty.
 */
public interface IPartySettings extends ConfigurationSerializable
{
	/**
	 * Apply the settings from another {@link IPartySettings}.
	 * @param settings The {@link IPartySettings} to copy the settings from.
	 */
	void apply(IPartySettings settings);
	/**
	 * Get the name of the party.
	 * @return The party name.
	 */
	String getName();
	/**
	 * Set the name of the party.
	 * @param name The new party name.
	 */
	void setName(String name);
	/**
	 * Get the party-wide teleportation setting.
	 * @return The teleportation setting for the whole party.
	 */
	boolean canTp();
	/**
	 * Set the party-wide teleportation setting.
	 * @param tp The new teleportation setting for the whole party.
	 */
	void setTp(boolean tp);
	/**
	 * Get the party-wide PvP setting.
	 * @return The PvP setting for the whole party.
	 */
	boolean canPvp();
	/**
	 * Set the party-wide PvP setting.
	 * @param pvp The new PvP setting for the whole party.
	 */
	void setPvp(boolean pvp);
	/**
	 * Checks if the party will be visible in party lists.
	 * @return Whether the party is visible in party lists.
	 */
	boolean isVisible();
	/**
	 * Changes whether the party will be visible in party lists.
	 * @param visible Whether the party should be visible in party lists.
	 */
	void setVisible(boolean visible);
	/**
	 * Checks whether the party has a shared inventory.
	 * @return <code>true</code> if the party has a shared inventory.
	 */
	boolean hasInventory();
	/**
	 * Changes whether the party has a shared inventory.
	 * @param enabled Whether the party should have a shared inventory.
	 */
	void setInventory(boolean enabled);
	/**
	 * Checks if the party requires an invitation ticket to join.
	 * @return <code>true</code> if the party is invite-only.
	 */
	boolean isInviteOnly();
	/**
	 * Changes whether the party requires and invitation ticket to join.
	 * @param only Whether the party should be invite-only.
	 */
	void setInviteOnly(boolean only);
	/**
	 * Checks if the party will never be automatically closed.
	 * @return <code>true</code> if the party is exempt from automatic closure.
	 */
	boolean isSticky();
	/**
	 * Sets whether the party will never be automatically closed.
	 * @param sticky Whether the party should be exempt from automatic closure.
	 */
	void setSticky(boolean sticky);
}
