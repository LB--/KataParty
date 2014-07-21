package com.lb_stuff.kataparty.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

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
	IMember addMember(UUID uuid);
	void removeMember(UUID uuid);
	IMember findMember(UUID uuid);
	IMember findMember(String name);
	int numMembers();
	Set<IMember> getMembersOnline();
	Set<IMember> getMembersAlive();
	Set<IMember> getMembersRanked(Rank r);
	void disband();
	boolean isDisbanded();
	void enableInventory();
	Inventory getInventory();
	void disableInventory(Player disabler);
	@Override @Deprecated
	void setInventory(boolean enabled);

	public static enum Rank
	{
		ADMIN,
		MODERATOR,
		MEMBER;
	}
	String rankName(Rank r);

	public interface IMember
	{
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
