package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IPartySettings;

public class PartySettings implements IPartySettings
{
	public PartySettings()
	{
	}
	public PartySettings(IPartySettings other)
	{
		name = other.getName();
		tp = other.canTp();
		pvp = other.canPvp();
		visible = other.isVisible();
		inv = other.hasInventory();
		invite = other.isInviteOnly();
		sticky = other.isSticky();
	}

	private String name = "KataParty";
	@Override
	public String getName()
	{
		return name;
	}
	@Override
	public void setName(String n)
	{
		name = n;
	}

	private boolean tp = true;
	@Override
	public boolean canTp()
	{
		return tp;
	}
	@Override
	public void setTp(boolean enabled)
	{
		tp = enabled;
	}

	private boolean pvp = false;
	@Override
	public boolean canPvp()
	{
		return pvp;
	}
	@Override
	public void setPvp(boolean enabled)
	{
		pvp = enabled;
	}

	private boolean visible = true;
	@Override
	public boolean isVisible()
	{
		return visible;
	}
	@Override
	public void setVisible(boolean enabled)
	{
		visible = enabled;
	}

	private boolean inv = false;
	@Override
	public boolean hasInventory()
	{
		return inv;
	}
	@Override
	public void setInventory(boolean enabled)
	{
		inv = enabled;
	}

	private boolean invite = false;
	@Override
	public boolean isInviteOnly()
	{
		return invite;
	}
	@Override
	public void setInviteOnly(boolean enabled)
	{
		invite = enabled;
	}

	private boolean sticky = false;
	@Override
	public boolean isSticky()
	{
		return sticky;
	}
	@Override
	public void setSticky(boolean enabled)
	{
		sticky = enabled;
	}
}
