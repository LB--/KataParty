package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.api.IPartyFactory.IMemberFactory;
import com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.UUID;

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
	 * Register an {@link IPartyFactory} for the given type of
	 * {@link IPartySettings} implementor.
	 * @param clazz The type of {@link IPartySettings} implementor
	 * for which to register the {@link IPartyFactory}.
	 * @param factory The {@link IPartyFactory} to be registered.
	 * @return The old {@link IPartyFactory} instance, or
	 * <code>null</code>.
	 */
	IPartyFactory registerPartyFactory(Class<? extends IPartySettings> clazz, IPartyFactory factory);
	/**
	 * Returns the {@link IPartyFactory} for the given type of
	 * {@link IPartySettings} implementor.
	 * @param clazz The type of {@link IPartySettings} implementor
	 * @return The {@link IPartyFactory} instance, or
	 * <code>null</code>.
	 */
	IPartyFactory getPartyFactory(Class<? extends IPartySettings> clazz);
	/**
	 * Register an {@link IMemberFactory} for the given type of
	 * {@link IMemberSettings} implementor.
	 * @param clazz The type of {@link ImemberSettings} implementor
	 * for which to register the {@link IMemberFactory}.
	 * @param factory The {@link IMemberFactory} to be registered.
	 * @return The old {@link IMemberFactory} instance, or
	 * <code>null</code>.
	 */
	IMemberFactory registerMemberFactory(Class<? extends IMemberSettings> clazz, IMemberFactory factory);
	/**
	 * Returns the {@link IMemberFactory} for the given type of
	 * {@link IMemberSettings} implementor.
	 * @param clazz The type of {@link IMemberSettings} implementor
	 * @return The {@link IMemberFactory} instance, or
	 * <code>null</code>.
	 */
	IMemberFactory getMemberFactory(Class<? extends IMemberSettings> clazz);
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
