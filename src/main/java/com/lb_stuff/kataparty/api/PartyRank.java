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

	/**
	 * Returns the name of this rank given the translations in the given
	 * (@link IMessenger}.
	 * @param messenger The {@link IMessenger} from which to take the name.
	 * @return The name of this rank given the translations in the given
	 * (@link IMessenger}.
	 */
	public String getName(IMessenger messenger)
	{
		switch(this)
		{
			case ADMIN:     return messenger.getMessage("party-rank-admin");
			case MODERATOR: return messenger.getMessage("party-rank-moderator");
			case MEMBER:    return messenger.getMessage("party-rank-member");
			default: throw new IllegalStateException();
		}
	}
}
