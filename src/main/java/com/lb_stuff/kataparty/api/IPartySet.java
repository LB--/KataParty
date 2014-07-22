package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Map;

/**
 * Holds and manages a {@link java.util.Set} of {@link IParty}s.
 * This interface should only be implemented by KataParty.
 */
public interface IPartySet extends Iterable<IParty>, ConfigurationSerializable
{
	/**
	 * Returns the {@link IMessenger} for these parties.
	 * @return The {@link IMessenger} for these parties.
	 */
	IMessenger getMessenger();
	/**
	 * Creates and adds a new {@link IParty} if the
	 * {@link com.lb_stuff.kataparty.api.event.PartyCreateEvent} is not
	 * cancelled.
	 * @param creator The {@link org.bukkit.entity.Player} that created
	 * this party, or <code>null</code>.
	 * @param settings The {@link IPartySettings} to create this party with.
	 * @return Thew newly created {@link IParty}, or <code>null</code> if the
	 * {@link com.lb_stuff.kataparty.api.event.PartyCreateEvent} was
	 * cancelled.
	 */
	IParty newParty(Player creator, IPartySettings settings);
	/**
	 * Adds an {@link IParty} implemented by another plugin.
	 * @param p The {@link IParty}.
	 * @return <code>true</code> if the {@link IParty} was actually added.
	 */
	boolean add(IParty p);
	/**
	 * Disbands an {@link IParty} via
	 * {@link IParty#disband(com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason, org.bukkit.entity.Player)}.
	 * @param p The {@link IParty} to disband.
	 * @param r The {@link com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason}.
	 * @param player The {@link org.bukkit.entity.Player} that disbanded
	 * this party, or <code>null</code>.
	 */
	void remove(IParty p, PartyDisbandEvent.Reason r, Player player);
	/**
	 * Changes whether {@link IParty}s with no {@link IParty.IMember}s are
	 * kept. If {@link IParty#isSticky()} returns <code>true</code>, then
	 * it will not be removed.
	 * @param keep Whether to keep empty parties. If <code>false</code>,
	 * this removes all parties that are currently empty.
	 */
	void keepEmptyParties(boolean keep);
	/**
	 * Returns whether empty {@link IParty}s are kept.
	 * @return Whether empty {@link IParty}s are kept.
	 */
	boolean keepEmptyParties();
	/**
	 * Asynchronously-accessed class that stores settings for
	 * each {@link IParty.IMember}.
	 * This interface should only be implemented by KataParty.
	 */
	interface IAsyncMemberSettings
	{
		/**
		 * Returns the name of the {@link IParty} the
		 * {@link IParty.IMember} is in.
		 * @return The name of the {@link IParty} the
		 * {@link IParty.IMember} is in.
		 */
		String getPartyName();
		/**
		 * Changes the name of the {@link IParty} the
		 * {@link IParty.IMember} is in. Called by
		 * {@link IParty#setName(java.lang.String)}.
		 * @param partyname The new name of the
		 * {@link IParty} the {@link IParty.IMember} is in.
		 */
		void setPartyName(String partyname);
		/**
		 * Returns the {@link ChatFilterPref} for the
		 * {@link IParty.IMember}.
		 * @return The {@link ChatFilterPref} for the
		 * {@link IParty.IMember}.
		 */
		ChatFilterPref getPref();
		/**
		 * Changes the {@link ChatFilterPref} for the
		 * {@link IParty.IMember}.
		 * @param cfp The new {@link ChatFilterPref} for
		 * the {@link IParty.IMember}.
		 */
		void setPref(ChatFilterPref cfp);
		/**
		 * Calls {@link ChatFilterPref#opposite()}.
		 */
		void togglePref();
	}
	/**
	 * Creates an {@link IAsyncMemberSettings} for the given {@link java.util.UUID}.
	 * Called by
	 * {@link IParty#newMember(java.util.UUID, com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent.Reason)}.
	 * @param uuid The {@link java.util.UUID} to create the {@link IAsyncMemberSettings} for.
	 * @param pname The name of the {@link IParty}.
	 */
	void addSettings(UUID uuid, String pname);
	/**
	 * Returns the {@link IAsyncMemberSettings} for the given {@link java.util.UUID}.
	 * @param uuid The {@link java.util.UUID}.
	 * @return The {@link IAsyncMemberSettings} for the given {@link java.util.UUID}.
	 */
	IAsyncMemberSettings getSettings(UUID uuid);
	/**
	 * Removes an {@link IAsyncMemberSettings} for the given {@link java.util.UUID}.
	 * Called by
	 * {@link IParty#removeMember(java.util.UUID, com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent.Reason)}.
	 * @param uuid The {@link java.util.UUID}.
	 */
	void removeSettings(UUID uuid);
	/**
	 * Returns a {@link java.util.Map} of {@link java.util.UUID} to
	 * {@link IAsyncMemberSettings}.
	 * @return a {@link java.util.Map} of {@link java.util.UUID} to
	 * {@link IAsyncMemberSettings}.
	 */
	public Iterable<Map.Entry<UUID, IAsyncMemberSettings>> getPartyMembers();
	/**
	 * Finds an {@link IParty.IMember} for the given {@link java.util.UUID}.
	 * @param uuid The {@link java.util.UUID} given by
	 * {@link org.bukkit.OfflinePlayer#getUniqueId()}.
	 * @return An {@link IParty.IMember} if found, or <code>null</code>.
	 */
	IParty.IMember findMember(UUID uuid);
	/**
	 * Finds an {@link IParty} with ti given name.
	 * @param name The name of the {@link IParty} to find.
	 * @return The {@link IParty} if found, or <code>null</code>.
	 */
	IParty findParty(String name);
	/**
	 * Checks if this contains the given {@link IParty}.
	 * @param p The {@link IParty}.
	 * @return <code>true</code> if this contains the given {@link IParty}.
	 */
	boolean contains(IParty p);
}
