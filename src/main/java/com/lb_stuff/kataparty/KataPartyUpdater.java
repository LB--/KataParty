package com.lb_stuff.kataparty;

import net.gravitydevelopment.updater.Updater;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class KataPartyUpdater extends Updater
{
	private final KataPartyPlugin inst;
	private final Updater.UpdateType type;
	public KataPartyUpdater(KataPartyPlugin plugin, File file, Updater.UpdateType t)
	{
		super(plugin, 81209, file, t, false);
		inst = plugin;
		type = t;
	}

	@Override
	public boolean shouldUpdate(final String current, final String potential)
	{
		String[] c = current.split("\\.");
		String[] p = potential.split("\\.");
		if(c.length < 3 || p.length < 3 || c.length != p.length)
		{
			return true;
		}
		for(int i = 0; i < c.length; ++i)
		{
			if(Integer.parseInt(c[i]) < Integer.parseInt(p[i]))
			{
				inst.getLogger().warning("Out of date version (new version is v"+potential+")");
				inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
				{
					inst.getServer().getPluginManager().registerEvents(new Listener()
					{
						@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
						public void onJoin(final PlayerJoinEvent e)
						{
							if(e.getPlayer().hasPermission("KataParty.update-notify"))
							{
								inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
								{
									inst.tell(e.getPlayer(), "A new update is available: v"+current+" -> v"+potential);
									if(type == Updater.UpdateType.DEFAULT)
									{
										inst.tell(e.getPlayer(), "It should be automatically installed next restart.");
									}
									else
									{
										inst.tell(e.getPlayer(), "It will need to be updated manually.");
									}
								}});
							}
						}
					}, inst);
				}});
				return true;
			}
		}
		inst.getLogger().info("Up to date.");
		return false;
	}
}
