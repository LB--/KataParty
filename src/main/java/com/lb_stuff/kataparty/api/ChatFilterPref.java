package com.lb_stuff.kataparty.api;

/**
 * Indicates whether an {@link IParty.IMember} prefers to speak
 * in and see party chat, or speak in and see global chat.
 */
public enum ChatFilterPref
{
	/**
	 * Prefer to speak in party chat, and filter global chat.
	 */
	PREFER_PARTY,
	/**
	 * Prefer to speak in global chat, and filter party chat.
	 */
	PREFER_GLOBAL;

	/**
	 * Returns the opposite setting of this setting.
	 * @return The opposite setting of this setting.
	 */
	public ChatFilterPref opposite()
	{
		switch(this)
		{
			case PREFER_PARTY: return PREFER_GLOBAL;
			case PREFER_GLOBAL: return PREFER_PARTY;
		}
		throw new IllegalStateException();
	}
}
