package com.lb_stuff.kataparty.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public abstract class SkullGenerator
{
	private SkullGenerator()
	{
	}

	public static ItemStack getPlayerSkull(UUID uuid)
	{
		ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		SkullMeta m = (SkullMeta)is.getItemMeta();
		try
		{
			m.getClass().getMethod("setOwner", UUID.class).invoke(m, uuid);
		}
		catch(NoSuchMethodException|IllegalAccessException|InvocationTargetException e)
		{
			m.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
		}
		is.setItemMeta(m);
		return is;
	}
}
