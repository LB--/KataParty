package com.lb_stuff.kataparty.api;

import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;

/**
 * ...
 */
public interface IGuiButton
{
	ItemStack display();
	void onClick(ClickType click);
}
