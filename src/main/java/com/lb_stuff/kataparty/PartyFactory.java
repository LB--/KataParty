package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.PartySettings.MemberSettings;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.IPartyFactory;
import com.lb_stuff.kataparty.api.IPartyFactory.IMemberFactory;
import com.lb_stuff.kataparty.api.IPartySet;
import com.lb_stuff.kataparty.api.IPartySettings;
import com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;

public class PartyFactory implements IPartyFactory
{
	@Override
	public IParty create(IPartySet parent, IPartySettings from)
	{
		if(from instanceof PartySettings)
		{
			return new Party(parent, from);
		}
		return null;
	}

	public class MemberFactory implements IMemberFactory
	{
		@Override
		public IParty.IMember create(IParty parent, IMemberSettings from)
		{
			if(from instanceof MemberSettings)
			{
				return new Party.Member(parent, from);
			}
			return null;
		}
	}
}
