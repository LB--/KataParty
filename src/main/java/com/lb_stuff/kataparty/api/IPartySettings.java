package com.lb_stuff.kataparty.api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface IPartySettings extends ConfigurationSerializable
{
	void apply(IPartySettings settings);
	String getName();
	void setName(String name);
	boolean canTp();
	void setTp(boolean tp);
	boolean canPvp();
	void setPvp(boolean pvp);
	boolean isVisible();
	void setVisible(boolean visible);
	boolean hasInventory();
	void setInventory(boolean enabled);
	boolean isInviteOnly();
	void setInviteOnly(boolean only);
	boolean isSticky();
	void setSticky(boolean sticky);
}
