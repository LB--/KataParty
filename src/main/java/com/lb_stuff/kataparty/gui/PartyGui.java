package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class PartyGui implements Listener
{
	protected final KataPartyPlugin inst;
	protected final Player player;
	private Inventory inv;
	public PartyGui(KataPartyPlugin plugin, Player p, int guirows, String guiname)
	{
		inst = plugin;
		player = p;
		inv = Bukkit.createInventory(null, 9*guirows, guiname);
	}
	public PartyGui(KataPartyPlugin plugin, Player p, Inventory inventory)
	{
		inst = plugin;
		player = p;
		inv = inventory;
	}
	protected final void rename(String guiname)
	{
		Inventory newinv = Bukkit.createInventory(null, inv.getSize(), guiname);
		newinv.setContents(inv.getContents());
		inv = newinv;
	}

	protected abstract void update();

	protected final void addButton(int slot, String name, Material icon, List<String> info)
	{
		ItemStack i = new ItemStack(icon);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(name);
		m.setLore(info);
		i.setItemMeta(m);
		inv.setItem(slot, i);
	}
	protected final void addButton(int slot, ItemStack button)
	{
		inv.setItem(slot, button);
	}
	protected final int getButton(int slot)
	{
		return inv.getItem(slot).getAmount();
	}
	protected final String getButtonName(int slot)
	{
		return inv.getItem(slot ) != null? inv.getItem(slot).getItemMeta().getDisplayName() : null;
	}
	protected final void setButton(int slot, int value)
	{
		ItemStack i = inv.getItem(slot);
		i.setAmount(value);
		inv.setItem(slot, i);
	}
	protected final void setButton(int slot, Material icon)
	{
		ItemStack i = inv.getItem(slot);
		i.setType(icon);
		inv.setItem(slot, i);
	}
	protected final void setButton(int slot, int value, Material icon)
	{
		ItemStack i = inv.getItem(slot);
		i.setType(icon);
		i.setAmount(value);
		inv.setItem(slot, i);
	}
	protected final void setButton(int slot, String name)
	{
		ItemStack i = inv.getItem(slot);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(name);
		i.setItemMeta(m);
		inv.setItem(slot, i);
	}
	protected final void setButton(int slot, List<String> info)
	{
		ItemStack i = inv.getItem(slot);
		ItemMeta m = i.getItemMeta();
		m.setLore(info);
		i.setItemMeta(m);
		inv.setItem(slot, i);
	}
	protected final void setButton(int slot, String name, List<String> info)
	{
		ItemStack i = inv.getItem(slot);
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(name);
		m.setLore(info);
		i.setItemMeta(m);
		inv.setItem(slot, i);
	}
	protected final void removeButton(int slot)
	{
		inv.clear(slot);
	}
	protected final void clearButtons()
	{
		inv.clear();
	}
	protected abstract void onButton(int slot, ClickType click);

	public final void show()
	{
		update();
		final PartyGui This = this;
		inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			player.closeInventory();
			inst.getServer().getPluginManager().registerEvents(This, inst);
			player.openInventory(inv);
		}});
	}
	public final void hide()
	{
		clearButtons();
		inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			player.closeInventory();
		}});
	}

	protected void onClose()
	{
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onInvClick(InventoryClickEvent e)
	{
		if(e.getWhoClicked() == player)
		{
			if(e.getRawSlot() < e.getView().getTopInventory().getSize())
			{
				e.setCancelled(true);
				if(!e.getSlotType().equals(SlotType.OUTSIDE) && inv.getItem(e.getSlot()) != null)
				{
					onButton(e.getSlot(), e.getClick());
				}
			}
			update();
			e.getView().getTopInventory().setContents(inv.getContents());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void OnInvDrag(InventoryDragEvent e)
	{
		if(e.getWhoClicked() == player)
		{
			for(Integer rawslot : e.getRawSlots())
			{
				if(rawslot < e.getView().getTopInventory().getSize())
				{
					e.setCancelled(true);
					return;
				}
			}
			update();
			e.getView().getTopInventory().setContents(inv.getContents());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public final void onInvClose(InventoryCloseEvent e)
	{
		if(e.getPlayer() == player)
		{
			HandlerList.unregisterAll(this);
			onClose();
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onKick(PlayerKickEvent e)
	{
		e.getPlayer().closeInventory();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e)
	{
		e.getPlayer().closeInventory();
	}
}
