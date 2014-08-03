package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.ChatFilterPref;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;
import com.lb_stuff.kataparty.api.event.PartySettingsChangeEvent;
import com.lb_stuff.service.ChatFilterService.AsyncMessage;

import static com.lb_stuff.kataparty.api.ChatFilterPref.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PartyChatFilter implements Listener
{
	private final KataPartyPlugin inst;
	public PartyChatFilter(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private class AsyncMemberSettings
	{
		private final UUID uuid;
		private String partyname;
		private ChatFilterPref pref = PREFER_PARTY;
		private boolean alone = true;
		public AsyncMemberSettings(UUID id, String pname)
		{
			uuid = id;
			partyname = pname;
		}

		public String getPartyName()
		{
			return partyname;
		}
		public void setPartyName(String partyname)
		{
			this.partyname = partyname;
		}

		public ChatFilterPref getPref()
		{
			return pref;
		}
		public void setPref(ChatFilterPref cfp)
		{
			pref = cfp;
			tellPref();
		}
		public void togglePref()
		{
			setPref(getPref().opposite());
		}
		public void tellPref()
		{
			Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
			{
				OfflinePlayer offp = Bukkit.getOfflinePlayer(uuid);
				if(offp.isOnline())
				{
					Player p = offp.getPlayer();
					switch(getPref())
					{
						case PREFER_GLOBAL:
						{
							inst.tellMessage(p, "chat-filtering-global", getSwap());
						} break;
						case PREFER_PARTY:
						{
							inst.tellMessage(p, "chat-filtering-party", getSwap());
						} break;
						default: throw new IllegalStateException();
					}
					inst.tellMessage(p, "chat-filtering-howto");
				}
			}});
		}

		public boolean isAlone()
		{
			return alone;
		}
		public void setAlone(boolean isalone)
		{
			alone = isalone;
		}
	}
	private final ConcurrentHashMap<UUID, AsyncMemberSettings> partiers = new ConcurrentHashMap<>();
	private AsyncMemberSettings addSettings(UUID uuid, String pname)
	{
		AsyncMemberSettings ms = new AsyncMemberSettings(uuid, pname);
		partiers.put(uuid, ms);
		return ms;
	}
	private AsyncMemberSettings getSettings(UUID uuid)
	{
		return partiers.get(uuid);
	}
	private void removeSettings(UUID uuid)
	{
		partiers.remove(uuid);
	}

	private String getConfig(String path)
	{
		return inst.getConfig().getString("chat-filtering."+path);
	}
	public String getPartyPrefix(ChatFilterPref pref)
	{
		if(pref != null) switch(pref)
		{
			case PREFER_PARTY: return getConfig("prefix-when-party-preferred");
			case PREFER_GLOBAL: return getConfig("prefix-when-global-preferred");
		}
		return "";
	}
	public String getFilterFormat()
	{
		return getConfig("filtered-chat-format");
	}
	public String getSwap()
	{
		return getConfig("swap-string");
	}
	public ChatFilterPref getDefaultFilterPref(String event)
	{
		final String path = "defaults."+event;
		final String setting = getConfig(path).toLowerCase();
		switch(setting)
		{
			case "party": return ChatFilterPref.PREFER_PARTY;
			case "global": return ChatFilterPref.PREFER_GLOBAL;
			default:
			{
				inst.getLogger().warning("Invalid config setting for chat-filtering."+path+": \""+setting+"\"");
			} break;
		}
		return ChatFilterPref.PREFER_PARTY;
	}
	public void togglePref(UUID uuid)
	{
		AsyncMemberSettings ms = getSettings(uuid);
		if(ms != null)
		{
			ms.togglePref();
		}
	}

	private void updateAlone(final IParty p)
	{
		inst.getServer().getScheduler().runTask(inst, new Runnable(){@Override public void run()
		{
			Set<IParty.IMember> online = p.getMembersOnline();
			for(IParty.IMember m : p)
			{
				AsyncMemberSettings ms = getSettings(m.getUuid());
				if(ms != null)
				{
					ms.setAlone(online.size() == 1);
				}
			}
		}});
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onMessage(final AsyncMessage m)
	{
		if(!(m.getSource() instanceof Player))
		{
			return;
		}

		Player source = (Player)m.getSource();
		boolean prefswap = m.getMessage().startsWith(getSwap());
		String sourceparty = null;
		ChatFilterPref sourcepref = null;
		AsyncMemberSettings sourcesettings = getSettings(source.getUniqueId());
		if(sourcesettings != null)
		{
			sourceparty = sourcesettings.getPartyName();
			sourcepref = sourcesettings.getPref();
			if(prefswap)
			{
				m.setMessage(m.getMessage().substring(getSwap().length()));
				sourcepref = sourcepref.opposite();
			}
		}

		if(!(m.getTarget() instanceof Player))
		{
			if(sourcepref == PREFER_PARTY)
			{
				m.setFormat(String.format(getPartyPrefix(PREFER_PARTY), sourceparty)+m.getFormat());
			}
		}
		else
		{
			Player target = (Player)m.getTarget();
			String targetparty = null;
			ChatFilterPref targetpref = null;
			AsyncMemberSettings targetsettings = getSettings(target.getUniqueId());
			if(targetsettings != null)
			{
				targetparty = targetsettings.getPartyName();
				targetpref = targetsettings.getPref();
			}

			final boolean sameparty = (targetparty != null && sourceparty != null && targetparty.equals(sourceparty));

			if(targetpref == PREFER_PARTY)
			{
				if(sourcepref == null || sourcepref == PREFER_GLOBAL)
				{
					m.setMessage(getFilterFormat()+m.getMessage());
				}
				else if(sameparty)
				{
					m.setFormat(String.format(getPartyPrefix(targetpref), sourceparty)+m.getFormat());
				}
				else
				{
					m.setCancelled(true);
				}
			}
			else if(targetpref == PREFER_GLOBAL)
			{
				if(sourcepref == PREFER_PARTY)
				{
					if(sameparty)
					{
						m.setFormat(String.format(getPartyPrefix(targetpref), sourceparty)+m.getFormat());
						m.setMessage(getFilterFormat()+m.getMessage());
					}
					else
					{
						m.setCancelled(true);
					}
				}
			}
			else if(sourcepref == PREFER_PARTY)
			{
				m.setCancelled(true);
			}

			if(sourcepref == PREFER_PARTY && sourcesettings.isAlone())
			{
				Bukkit.getScheduler().runTask(inst, new Runnable(){@Override public void run()
				{
					inst.tellMessage((Player)m.getSource(), "chat-filtering-alone", getSwap());
				}});
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
		if(m != null)
		{
			inst.tellMessage(p, "party-member-inform", m.getParty().getName());
			AsyncMemberSettings ms = getSettings(m.getUuid());
			if(ms == null)
			{
				ms = addSettings(m.getUuid(), m.getParty().getName());
			}
			ms.setPref(getDefaultFilterPref("on-join-server"));
			updateAlone(m.getParty());
		}
		else
		{
			inst.tellMessage(p, "party-introvert-inform");
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent e)
	{
		final IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			updateAlone(m.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		final IParty.IMember m = inst.getPartySet().findMember(e.getPlayer().getUniqueId());
		if(m != null)
		{
			updateAlone(m.getParty());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberJoin(PartyMemberJoinEvent e)
	{
		AsyncMemberSettings ms = addSettings(e.getApplicant().getUuid(), e.getParty().getName());
		if(e.getReason() == PartyMemberJoinEvent.Reason.CREATOR)
		{
			ms.setPref(getDefaultFilterPref("on-party-create"));
		}
		else
		{
			ms.setPref(getDefaultFilterPref("on-party-join"));
		}
		updateAlone(e.getParty());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		removeSettings(e.getMember().getUuid());
		updateAlone(e.getMember().getParty());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRename(PartySettingsChangeEvent e)
	{
		final String oldname = e.getParty().getName();
		final String newname = e.getChanges().getName();
		if(!oldname.equals(newname))
		{
			for(Map.Entry<UUID, AsyncMemberSettings> m : partiers.entrySet())
			{
				if(e.getParty().findMember(m.getKey()) != null)
				{
					m.getValue().setPartyName(newname);
				}
			}
		}
	}
}
