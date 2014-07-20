package com.lb_stuff.kataparty.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Set;

public interface IParty extends Iterable<IParty.IMember>
{
	String getName();
	void rename(String name);
	@Override
	int hashCode();
	@Override
	boolean equals(Object obj);
	IMember addMember(UUID uuid);
	void removeMember(UUID uuid);
	IMember findMember(UUID uuid);
	IMember findMember(String name);
	int numMembers();
	Set<IMember> getMembersOnline();
	Set<IMember> getMembersAlive();
	Set<IMember> getMembersRanked(Rank r);
	void disband();
	boolean canTp();
	void setTp(boolean tp);
	boolean canPvp();
	void setPvp(boolean pvp);
	boolean isVisible();
	void setVisible(boolean visible);
	void enableInventory();
	Inventory getInventory();
	void disableInventory(Player disabler);
	boolean isInviteOnly();
	void setInviteOnly(boolean only);
	boolean isSticky();
	void setSticky(boolean sticky);

	public static enum Rank
	{
		ADMIN,
		MODERATOR,
		MEMBER;
	}
	String rankName(Rank r);

	public interface IMember
	{
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
