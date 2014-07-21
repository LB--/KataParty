package com.lb_stuff.kataparty.api;

public enum ChatFilterPref
{
	PREFER_PARTY,
	PREFER_GLOBAL;

	public ChatFilterPref opposite()
	{
		switch(this)
		{
			case PREFER_PARTY: return PREFER_GLOBAL;
			case PREFER_GLOBAL: return PREFER_PARTY;
		}
		throw new IllegalArgumentException();
	}
}
