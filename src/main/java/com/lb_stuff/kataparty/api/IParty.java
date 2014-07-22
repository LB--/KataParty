package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.api.event.PartyDisbandEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Location;

import java.util.UUID;
import java.util.Set;

/**
 * Represents a party.
 * This interface may be implemented by plugins other than KataParty.
 */
public interface IParty extends Iterable<IParty.IMember>, IPartySettings
{
	/**
	 * Should return the hash code of {@link IPartySettings#getName()} converted to lower case
	 * via {@link java.lang.String#toLowerCase()}.
	 * @return The hash code of {@link IPartySettings#getName()} converted to lower case
	 * via {@link java.lang.String#toLowerCase()}.
	 */
	@Override
	int hashCode();
	/**
	 * <code>obj</code> may be an {@link IPartySettings} or {@link java.lang.String} for which the name
	 * is compared to <code>this.</code>{@link IPartySettings#getName()}
	 * via {@link java.lang.String#equalsIgnoreCase(java.lang.String)}
	 * @param obj
	 * @return Whether this party has the same name as the given {@link IPartySettings} or {@link java.lang.String}
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Calls {@link Messenger#tell(org.bukkit.entity.Player, java.lang.String)} for each online member.
	 * @param message The string to send to each member.
	 */
	void informMembers(String message);
	/**
	 * Calls {@link Messenger#tellMessage(org.bukkit.entity.Player, java.lang.String, java.lang.Object...)}
	 * for each online member.
	 * @param name The name of the message in the config file.
	 * @param parameters Zero or more parameters as required by the message.
	 */
	void informMembersMessage(String name, Object... parameters);
	/**
	 * Retrieves the {@link IPartySet} this party is in.
	 * @return The {@link IPartySet} this party is in.
	 */
	IPartySet getPartySet();
	/**
	 * Fires {@link com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent}
	 * and adds a new {@link IMember} to this party.
	 * @param uuid The {@link java.util.UUID} of the {@link org.bukkit.OfflinePlayer}.
	 * @param r The {@link com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent.Reason}.
	 * @return The {@link IMember}, or <code>null</code> if the
	 * {@link com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent} was cancelled.
	 */
	IMember newMember(UUID uuid, PartyMemberJoinEvent.Reason r);
	/**
	 * Adds an {@link IMember} from another plugin and calls {@link IMember#setParty(com.lb_stuff.kataparty.api.IParty)}.
	 * @param m The {@link IMember}.
	 */
	void add(IMember m);
	/**
	 * Removes an {@link IMember} based on {@link java.util.UUID}. If
	 * {@link IPartySet#keepEmptyParties()} is <code>false</code> and
	 * {@link #isSticky()} is also <code>false<code>, then this will call
	 * {@link #disband(com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason, org.bukkit.entity.Player)}
	 * when the last {@link IMember} is removed.
	 * @param uuid The {@link java.util.UUID} of the {@link org.bukkit.OfflinePlayer}.
	 * @param r The {@link com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent.Reason}.
	 */
	void removeMember(UUID uuid, PartyMemberLeaveEvent.Reason r);
	/**
	 * Finds an {@link IMember} based on {@link java.util.UUID}.
	 * @param uuid The {@link java.util.UUID} of the {@link org.bukkit.OfflinePlayer}.
	 * @return The {@link IMember}, or <code>null</code> if not found.
	 */
	IMember findMember(UUID uuid);
	/**
	 * Finds an {@link IMember} based on the {@link java.util.UUID} of
	 * {@link org.bukkit.Bukkit#getOfflinePlayer(java.lang.String)}.
	 * @param name The name of the {@link org.bukkit.OfflinePlayer}.
	 * @return The {@link IMember}, or <code>null</code> if not found.
	 */
	IMember findMember(String name);
	/**
	 * Returns the number of {@link IMember}s in this party.
	 * @return The number of {@link IMember}s in this party.
	 */
	int numMembers();
	/**
	 * Generates a {@link java.util.Set} of {@link IMember}s for which
	 * {@link org.bukkit.OfflinePlayer#isOnline()} returns <code>true</code>.
	 * @return A {@link java.util.Set} of online {@link IMember}s.
	 */
	Set<IMember> getMembersOnline();
	/**
	 * Generates a {@link java.util.Set} of {@link IMember}s for which
	 * {@link org.bukkit.entity.Player#isDead()} returns <code>false</code>.
	 * @return A {@link java.util.Set} of alive {@link IMember}s.
	 */
	Set<IMember> getMembersAlive();
	/**
	 * Generates a {@link java.util.Set} of {@link IMember}s for which
	 * {@link IMember#getRank()} equals <code>r</code>.
	 * @param r The rank criteria.
	 * @return A {@link java.util.Set} of {@link IMember}s with rank <code>r</code>.
	 */
	Set<IMember> getMembersRanked(Rank r);
	/**
	 * Permanently disbands this party.
	 * @param r The {@link com.lb_stuff.kataparty.api.event.PartyDisbandEvent.Reason}.
	 * @param p The {@link org.bukkit.entity.Player} that disbanded the party or <code>null</code>.
	 * @return Whether the party was actually disbanded.
	 */
	boolean disband(PartyDisbandEvent.Reason r, Player p);
	/**
	 * Checks if this party is permanently disbanded.
	 * @return Whether this party is permanently disbanded.
	 */
	boolean isDisbanded();
	/**
	 * Enables the shared inventory for this party.
	 * Preferred over {@link IPartySettings#setInventory(boolean)}.
	 */
	void enableInventory();
	/**
	 * Returns the party-wide {@link org.bukkit.inventory.Inventory}.
	 * @return The party-wide {@link org.bukkit.inventory.Inventory}.
	 */
	Inventory getInventory();
	/**
	 * Disables the shared inventory for this party and drops all contained items
	 * at the given {@link org.bukkit.Location}.
	 * @param droploc The {@link org.bukkit.Location} to drop all contained items. If
	 * null, uses the spawn of world 0.
	 */
	void disableInventory(Location droploc);
	/**
	 * Calls either {@link #enableInventory()} or {@link #disableInventory(org.bukkit.Location)}
	 * with a <code>null</code> location.
	 * @param enabled Whether to enable or disable the shared inventory.
	 * @deprecated Calls {@link #disableInventory(org.bukkit.Location)} with <code>null</code>.
	 */
	@Override @Deprecated
	void setInventory(boolean enabled);

	/**
	 * The in-party rank of an {@link IMember}.
	 */
	public static enum Rank
	{
		/**
		 * The {@link IMember} has full control over the party.
		 */
		ADMIN,
		/**
		 * The {@link IMember} has limited control over the party.
		 */
		MODERATOR,
		/**
		 * The {@link IMember} can only change their personal settings.
		 */
		MEMBER;
	}
	/**
	 * Retrieves the user-defined name of the given {@link Rank} from the config file via
	 * {@link Messenger#getMessage(java.lang.String, java.lang.Object...)}.
	 * @param r The {@link Rank} to retrieve the user-defined name of.
	 * @return
	 */
	String rankName(Rank r);

	/**
	 * A member of an {@link IParty}.
	 * This interface may be implemented by plugins other than KataParty.
	 */
	public interface IMember extends ConfigurationSerializable
	{
		/**
		 * Used during deserialization and by {@link IParty#add(com.lb_stuff.kataparty.api.IParty.IMember)}.
		 * @param p The {@link IParty}.
		 * @deprecated Should not be used in normal circumstances.
		 */
		@Deprecated
		void setParty(IParty p);
		/**
		 * If this member is online, sends a message to them via {@link IPartySet#getMessenger()}.
		 * Used by {@link IParty#informMembers(java.lang.String)}.
		 * @param message The message to send to this member if they are online.
		 */
		void inform(String message);
		/**
		 * If this member is online, sends a user-defined message from the config to them
		 * via {@link IPartySet#getMessenger()}.
		 * Used by {@link IParty#informMembersMessage(java.lang.String, java.lang.Object...)}.
		 * @param name The name of the message.
		 * @param parameters Zero or more parameters as required by the message.
		 */
		void informMessage(String name, Object... parameters);
		/**
		 * Returns {@link java.util.UUID#hashCode()}.
		 * @return {@link java.util.UUID#hashCode()}.
		 */
		@Override
		int hashCode();
		/**
		 * Calls {@link java.util.UUID#equals(java.lang.Object)}.
		 * @param obj The {@link IMember} or {@link java.util.UUID}.
		 * @return {@link java.util.UUID#equals(java.lang.Object)}.
		 */
		@Override
		boolean equals(Object obj);
		/**
		 * Returns the {@link IParty} this is a member of.
		 * @return The {@link IParty} this is a member of.
		 */
		IParty getParty();
		/**
		 * Returns this member's {@link java.util.UUID}.
		 * @return This member's {@link java.util.UUID}.
		 */
		UUID getUuid();
		/**
		 * Returns this member's {@link Rank}.
		 * @return This member's {@link Rank}.
		 */
		Rank getRank();
		/**
		 * Calls {@link IParty#rankName(com.lb_stuff.kataparty.api.IParty.Rank)}.
		 * @return {@link IParty#rankName(com.lb_stuff.kataparty.api.IParty.Rank)}.
		 */
		String getRankName();
		/**
		 * Sets this member's {@link Rank}.
		 * @param r The new {@link Rank} this member should have.
		 */
		void setRank(Rank r);
		/**
		 * Returns this member's personal teleportation setting.
		 * @return This member's personal teleportation setting.
		 */
		boolean canTp();
		/**
		 * Sets this member's personal teleportation setting.
		 * @param tp The new value for this member's personal teleportation setting.
		 */
		void setTp(boolean tp);
	}
}
