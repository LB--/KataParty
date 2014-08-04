package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.KataPartyService;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;
import com.lb_stuff.kataparty.api.event.PartySettingsChangeEvent;

import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static org.bukkit.ChatColor.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*default*/ class PartyNametagListener implements OptionalComponent, Listener
{
	private final KataPartyPlugin inst;
	public PartyNametagListener(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private boolean started = false;
	@Override
	public boolean isStarted()
	{
		return started;
	}
	@Override
	public void start()
	{
		if(isStarted())
		{
			return;
		}
		for(IParty.IMember m : inst.getService().getOnlineMembers())
		{
			partiers.put(m.getUuid(), m.getParty().getName());
		}
		Bukkit.getPluginManager().registerEvents(this, inst);
		refreshAll();
		started = true;
	}
	@Override
	public void stop()
	{
		if(!isStarted())
		{
			return;
		}
		HandlerList.unregisterAll(this);
		partiers.clear();
		refreshAll();
		started = false;
	}

	private final ConcurrentHashMap<UUID, String> partiers = new ConcurrentHashMap<>();
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onNametagReceive(AsyncPlayerReceiveNameTagEvent e)
	{
		String pof = partiers.get(e.getNamedPlayer().getUniqueId());
		String pfor = partiers.get(e.getPlayer().getUniqueId());
		if(pof != null && pfor != null)
		{
			if(pof.equals(pfor))
			{
				e.setTag(""+GREEN+e.getTag());
			}
			else
			{
				e.setTag(""+RED+e.getTag());
			}
		}
	}

	public void refreshAll()
	{
		Set<Player> members = KataPartyService.getOnlinePlayers(inst.getService().getOnlineMembers());
		for(Player p : members)
		{
			TagAPI.refreshPlayer(p, members);
		}
	}
	public void refreshFor(UUID uuid)
	{
		OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
		if(offp.isOnline())
		{
			final Player onp = offp.getPlayer();
			final Set<Player> members = KataPartyService.getOnlinePlayers(inst.getService().getOnlineMembers());
			Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
			{
				TagAPI.refreshPlayer(onp, members);
			}});
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null)
		{
			partiers.put(m.getUuid(), m.getParty().getName());
			refreshFor(m.getUuid());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberJoin(PartyMemberJoinEvent e)
	{
		partiers.put(e.getApplicant().getUuid(), e.getParty().getName());
		refreshFor(e.getApplicant().getUuid());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		partiers.remove(e.getMember().getUuid());
		refreshFor(e.getMember().getUuid());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRename(PartySettingsChangeEvent e)
	{
		final String oldname = e.getParty().getName();
		final String newname = e.getChanges().getName();
		if(!oldname.equals(newname))
		{
			for(Map.Entry<UUID, String> m : partiers.entrySet())
			{
				if(e.getParty().findMember(m.getKey()) != null)
				{
					m.setValue(newname);
				}
			}
		}
	}
}
