package com.lb_stuff.kataparty.api;

/**
 * The in-party rank of an {@link IParty.IMember}.
 */
public enum PartyRank
{
	/**
	 * The {@link IParty.IMember} has full control over the party.
	 */
	ADMIN,
	/**
	 * The {@link IParty.IMember} has limited control over the party.
	 */
	MODERATOR,
	/**
	 * The {@link IParty.IMember} can only change their personal settings.
	 */
	MEMBER;
}
