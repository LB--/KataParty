package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.api.IGuiButton;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.List;

public abstract class GenericGuiButton implements IGuiButton
{
	private final ItemStack button;
	public GenericGuiButton(String name, Material icon, List<String> info)
	{
		button = new ItemStack(icon);
		ItemMeta m = button.getItemMeta();
		m.setDisplayName(name);
		m.setLore(info);
		button.setItemMeta(m);
	}
	public GenericGuiButton(ItemStack is)
	{
		button = new ItemStack(is);
	}

	protected final int getValue()
	{
		return button.getAmount();
	}
	protected final String getName()
	{
		return button.getItemMeta().getDisplayName();
	}
	protected final void setValue(int v)
	{
		button.setAmount(v);
	}
	protected final void setIcon(Material icon)
	{
		button.setType(icon);
	}
	protected final void setName(String name)
	{
		ItemMeta m = button.getItemMeta();
		m.setDisplayName(name);
		button.setItemMeta(m);
	}
	protected final void setLore(List<String> lore)
	{
		ItemMeta m = button.getItemMeta();
		m.setLore(lore);
		button.setItemMeta(m);
	}

	@Override
	public ItemStack display()
	{
		return new ItemStack(button);
	}
}
