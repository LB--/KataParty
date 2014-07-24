package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.api.IGuiButton;

import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CycleableGuiButton implements IGuiButton
{
	private final List<ItemStack> states = new ArrayList<>();
	private int index = 0;
	public CycleableGuiButton(ItemStack... cycle)
	{
		if(cycle.length < 1)
		{
			throw new IllegalArgumentException("Must have at least 1 state in cycle");
		}
		for(ItemStack is : cycle)
		{
			states.add(new ItemStack(is));
		}
	}

	@Override
	public ItemStack display()
	{
		return new ItemStack(states.get(index));
	}
	@Override
	public void onClick(ClickType click)
	{
		switch(click)
		{
			case LEFT:
			{
				if(++index >= states.size())
				{
					index = 0;
				}
			} break;
			case RIGHT:
			{
				if(--index < 0)
				{
					index = states.size()-1;
				}
			} break;
			default: break;
		}
	}

}
