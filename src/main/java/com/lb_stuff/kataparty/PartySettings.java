package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IPartySettings;

import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.Map;
import java.util.HashMap;

public class PartySettings implements IPartySettings
{
	static
	{
		ConfigurationSerialization.registerClass(PartySettings.class);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<>();
		data.put("name", getName());
		data.put("tp", canTp());
		data.put("pvp", canPvp());
		data.put("visible", isVisible());
		data.put("inv", hasInventory());
		data.put("invite", isInviteOnly());
		data.put("sticky", isSticky());
		return data;
	}
	public static PartySettings deserialize(Map<String, Object> data)
	{
		PartySettings s = new PartySettings();
		s.name = (String)data.get("name");
		s.tp = (Boolean)data.get("tp");
		s.pvp = (Boolean)data.get("pvp");
		s.visible = (Boolean)data.get("visible");
		s.inv = (Boolean)data.get("inv");
		s.invite = (Boolean)data.get("invite");
		s.sticky = (Boolean)data.get("sticky");
		return s;
	}

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

	@Override
	public void apply(IPartySettings s)
	{
		name = s.getName();
		tp = s.canTp();
		pvp = s.canPvp();
		visible = s.isVisible();
		inv = s.hasInventory();
		invite = s.isInviteOnly();
		sticky = s.isSticky();
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
