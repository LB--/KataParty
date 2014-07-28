package com.lb_stuff.kataparty.gui;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IGuiButton;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public abstract class PartyGui implements InventoryHolder, Listener
{
	protected final KataPartyPlugin inst;
	protected final Player player;
	private Inventory inv;
	public PartyGui(KataPartyPlugin plugin, Player p, int guirows, String guiname)
	{
		inst = plugin;
		player = p;
		inv = Bukkit.createInventory(this, 9*guirows, guiname);
	}
	public PartyGui(KataPartyPlugin plugin, Player p, Inventory inventory)
	{
		inst = plugin;
		player = p;
		inv = inventory;
		rename(inv.getTitle());
	}
	protected final void rename(String guiname)
	{
		Inventory newinv = Bukkit.createInventory(this, inv.getSize(), guiname);
		newinv.setContents(inv.getContents());
		inv = newinv;
	}

	@Override
	public Inventory getInventory()
	{
		return inv;
	}

	private final Map<Integer, IGuiButton> buttons = new HashMap<>();
	protected final void addButton(int slot, IGuiButton button)
	{
		buttons.put(slot, button);
	}
	protected final IGuiButton getButton(int slot)
	{
		return buttons.get(slot);
	}
	protected final Map<Integer, IGuiButton> getButtons()
	{
		Map<Integer, IGuiButton> clone = new HashMap<>();
		clone.putAll(buttons);
		return clone;
	}
	protected final void removeButton(int slot)
	{
		buttons.remove(slot);
	}
	protected final void clearButtons()
	{
		buttons.clear();
	}

	protected abstract void onUpdate();
	protected final void update()
	{
		onUpdate();
		inv.clear();
		for(Map.Entry<Integer, IGuiButton> e : buttons.entrySet())
		{
			inv.setItem(e.getKey(), e.getValue().display());
		}
	}

	public final void show()
	{
		update();
		inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			player.closeInventory();
			inst.getServer().getPluginManager().registerEvents(PartyGui.this, inst);
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
		if(e.getWhoClicked().getUniqueId().equals(player.getUniqueId()))
		{
			if(e.getRawSlot() < e.getView().getTopInventory().getSize())
			{
				e.setCancelled(true);
				if(!e.getSlotType().equals(SlotType.OUTSIDE) && inv.getItem(e.getSlot()) != null)
				{
					IGuiButton b = getButton(e.getSlot());
					if(b != null)
					{
						b.onClick(e.getClick());
					}
				}
			}
			update();
			e.getView().getTopInventory().setContents(inv.getContents());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void OnInvDrag(InventoryDragEvent e)
	{
		if(e.getWhoClicked().getUniqueId().equals(player.getUniqueId()))
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
		if(e.getPlayer().getUniqueId().equals(player.getUniqueId()))
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
