package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.api.IGuiButton;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.Arrays;

public class GenericGuiButton implements IGuiButton
{
	private final ItemStack button;
	public GenericGuiButton(Material icon)
	{
		button = new ItemStack(icon);
	}
	public GenericGuiButton(String name, Material icon, List<String> lore)
	{
		button = new ItemStack(icon);
		ItemMeta m = button.getItemMeta();
		m.setDisplayName(name);
		m.setLore(lore);
		button.setItemMeta(m);
	}
	public GenericGuiButton(String name, Material icon, String... lore)
	{
		this(name, icon, Arrays.asList(lore));
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
	protected final void setLore(String... lore)
	{
		setLore(Arrays.asList(lore));
	}

	protected ItemStack getItem()
	{
		return button;
	}

	@Override
	public ItemStack display()
	{
		return new ItemStack(button);
	}
	@Override
	public boolean onClick(ClickType click)
	{
		return true;
	}
}
