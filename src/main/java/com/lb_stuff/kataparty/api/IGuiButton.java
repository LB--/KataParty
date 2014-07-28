package com.lb_stuff.kataparty.api;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item in an inventory GUI that can be clicked
 * to perform some action.
 */
public interface IGuiButton
{
	/**
	 * Generate an updated display fore the button.
	 * @return The {@link ItemStack} to display.
	 */
	ItemStack display();
	/**
	 * Called upon clicking the button.
	 * @param click The {@link ClickType}.
	 * @return whether the click was allowed.
	 */
	boolean onClick(ClickType click);
}
