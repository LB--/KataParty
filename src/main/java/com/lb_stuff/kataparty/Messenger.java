package com.lb_stuff.kataparty;

import org.bukkit.entity.Player;

public interface Messenger
{
	String getMessage(String name, Object... parameters);
	@Deprecated
	void tell(Player p, String message);
	void tellMessage(Player p, String name, Object... parameters);
}
