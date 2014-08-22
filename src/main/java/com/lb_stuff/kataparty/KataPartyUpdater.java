package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.Perms;

import net.gravitydevelopment.updater.Updater;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;

import static org.bukkit.ChatColor.*;

import java.io.File;

public class KataPartyUpdater extends Updater
{
	public KataPartyUpdater(final KataPartyPlugin inst, File file, Updater.UpdateType t)
	{
		super(inst, 81209, file, t, new Updater.UpdateCallback(){@Override public void onFinish(final Updater u)
		{
			switch(u.getResult())
			{
				case SUCCESS:
				{
					inst.tellConsole("Updater: "+u.getLatestName()+" installed for next restart!");
				} break;
				case UPDATE_AVAILABLE:
				{
					inst.tellConsole("Updater: "+u.getLatestName()+" is available for download!");
					final PluginDescriptionFile d = inst.getDescription();
					inst.getServer().getPluginManager().registerEvents(new Listener()
					{
						@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
						public void onJoin(final PlayerJoinEvent e)
						{
							if(Perms.updateInform(e.getPlayer()))
							{
								inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
								{
									inst.tell(e.getPlayer(), "A new update is available: v"+d.getVersion()+" -> "+u.getLatestName());
									inst.tell(e.getPlayer(), "It will need to be updated manually:");
									inst.tell(e.getPlayer(), ""+AQUA+UNDERLINE+u.getLatestFileLink());
								}});
							}
						}
					}, inst);
				} break;
				case NO_UPDATE:
				{
					inst.tellConsole("Updater: Up to date!");
				} break;
				case DISABLED:
				{
					inst.tellConsole("Updater: Update checking disabled.");
				} break;
				case FAIL_DBO:
				{
					inst.tellConsole("Updater: Failed to check for updates.");
				} break;
				case FAIL_DOWNLOAD:
				{
					inst.tellConsole("Updater: Failed to download update.");
				} break;
				case FAIL_NOVERSION:
				{
					inst.tellConsole("Updater: Something is wrong with the download names on BukkitDev?");
				} break;
				case FAIL_BADID:
				{
					inst.tellConsole("Updater: The BukkitDev project seems to not exist?");
				} break;
				case FAIL_APIKEY:
				{
					inst.tellConsole("Updater: Invalid API Key?");
				} break;
				default:
				{
					inst.tellConsole("Updater: Unknown status");
				} break;
			}
		}});
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
			final int a = Integer.parseInt(c[i]);
			final int b = Integer.parseInt(p[i]);
			if(a == b)
			{
				continue;
			}
			else if(a > b)
			{
				break;
			}
			else //if(a < b)
			{
				return true;
			}
		}
		return false;
	}
}
