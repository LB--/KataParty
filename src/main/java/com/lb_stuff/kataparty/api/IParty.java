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

public interface IParty extends Iterable<IParty.IMember>, IPartySettings
{
	@Override
	int hashCode();
	@Override
	boolean equals(Object obj);

	void informMembers(String message);
	void informMembersMessage(String name, Object... parameters);
	IPartySet getPartySet();
	IMember newMember(UUID uuid, PartyMemberJoinEvent.Reason r);
	void add(IMember m);
	void removeMember(UUID uuid, PartyMemberLeaveEvent.Reason r);
	IMember findMember(UUID uuid);
	IMember findMember(String name);
	int numMembers();
	Set<IMember> getMembersOnline();
	Set<IMember> getMembersAlive();
	Set<IMember> getMembersRanked(Rank r);
	boolean disband(PartyDisbandEvent.Reason r, Player p);
	boolean isDisbanded();
	void enableInventory();
	Inventory getInventory();
	void disableInventory(Location droploc);
	@Override @Deprecated
	void setInventory(boolean enabled);

	public static enum Rank
	{
		ADMIN,
		MODERATOR,
		MEMBER;
	}
	String rankName(Rank r);

	public interface IMember extends ConfigurationSerializable
	{
		@Deprecated
		void setParty(IParty p);
		void inform(String message);
		void informMessage(String name, Object... parameters);
		@Override
		int hashCode();
		@Override
		boolean equals(Object obj);
		IParty getParty();
		UUID getUuid();
		Rank getRank();
		String getRankName();
		void setRank(Rank r);
		boolean canTp();
		void setTp(boolean tp);
	}
}
