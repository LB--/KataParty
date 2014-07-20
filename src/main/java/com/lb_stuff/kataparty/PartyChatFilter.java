package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.PartySet.MemberSettings;
import static com.lb_stuff.kataparty.ChatFilterPref.*;
import com.lb_stuff.kataparty.api.IParty;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import java.util.UUID;

public class PartyChatFilter implements Listener
{
	private final KataPartyPlugin inst;
	public PartyChatFilter(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	private MemberSettings getSettings(UUID uuid)
	{
		return inst.getParties().getSettings(uuid);
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

	public void tellFilterPref(Player p)
	{
		MemberSettings ms = inst.getParties().getSettings(p.getUniqueId());
		if(ms != null)
		{
			switch(ms.getPref())
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
		else
		{
			inst.tellMessage(p, "not-in-party");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) //highest executed last
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		String msg = e.getMessage();
		String fmt = e.getFormat();
		Player source = e.getPlayer();
		Set<Player> targets = e.getRecipients();

		boolean prefswap = msg.startsWith(getSwap());
		String sourceparty = null;
		ChatFilterPref sourcepref = null;
		MemberSettings sourcesettings = getSettings(source.getUniqueId());
		if(sourcesettings != null)
		{
			sourceparty = sourcesettings.getPartyName();
			sourcepref = sourcesettings.getPref();
		}

		if(sourcesettings != null)
		{
			if(prefswap)
			{
				msg = msg.substring(getSwap().length());
			}
		}

		final String normalmsg = String.format(fmt, source.getDisplayName(), msg);
		final String filtermsg = String.format(fmt, source.getDisplayName(), getFilterFormat()+msg);

		//need to manually send to console since we are cancelling the event
		if(sourcepref != null)
		{
			if(sourcepref.equals(PREFER_GLOBAL))
			{
				if(prefswap)
				{
					inst.getServer().getConsoleSender().sendMessage
					(
						String.format(getPartyPrefix(PREFER_PARTY), sourceparty)+normalmsg
					);
				}
				else
				{
					inst.getServer().getConsoleSender().sendMessage(normalmsg);
				}
			}
			else if(sourcepref.equals(PREFER_PARTY))
			{
				if(prefswap)
				{
					inst.getServer().getConsoleSender().sendMessage(normalmsg);
				}
				else
				{
					inst.getServer().getConsoleSender().sendMessage
					(
						String.format(getPartyPrefix(PREFER_PARTY), sourceparty)+normalmsg
					);
				}
			}
		}
		else
		{
			inst.getServer().getConsoleSender().sendMessage(normalmsg);
		}

		for(Player target : targets)
		{
			String targetparty = null;
			ChatFilterPref targetpref = null;
			MemberSettings targetsettings = getSettings(target.getUniqueId());
			if(targetsettings != null)
			{
				targetparty = targetsettings.getPartyName();
				targetpref = targetsettings.getPref();
			}

			if(sourcepref == null)
			{
				if(targetpref == null || targetpref.equals(PREFER_GLOBAL))
				{
					target.sendMessage(normalmsg);
				}
				else
				{
					target.sendMessage(filtermsg);
				}
			}
			else if(sourcepref.equals(PREFER_PARTY))
			{
				if(prefswap)
				{
					if(targetpref == null || targetpref.equals(PREFER_GLOBAL))
					{
						target.sendMessage(normalmsg);
					}
					else if(targetpref.equals(PREFER_PARTY))
					{
						target.sendMessage(filtermsg);
					}
				}
				else if(targetpref != null && targetparty.equals(sourceparty))
				{
					target.sendMessage(String.format(getPartyPrefix(targetpref), sourceparty)+normalmsg);
				}
			}
			else if(sourcepref.equals(PREFER_GLOBAL))
			{
				if(!prefswap)
				{
					if(targetpref == null || targetpref.equals(PREFER_GLOBAL))
					{
						target.sendMessage(normalmsg);
					}
					else if(targetpref.equals(PREFER_PARTY))
					{
						target.sendMessage(filtermsg);
					}
				}
				else if(targetpref != null && targetparty.equals(sourceparty))
				{
					target.sendMessage(String.format(getPartyPrefix(targetpref), sourceparty)+filtermsg);
				}
			}
		}

		e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		IParty.IMember m = inst.getParties().findMember(p.getUniqueId());
		if(m != null)
		{
			inst.tellMessage(p, "party-member-inform", m.getParty().getName());
			MemberSettings ms = getSettings(p.getUniqueId());
			if(ms != null)
			{
				ms.setPref(getDefaultFilterPref("on-join-server"));
				tellFilterPref(p);
			}
			else
			{
				inst.getLogger().warning(p.getName()+" does not have a MemberSettings but they're in a party!");
			}
		}
		else
		{
			inst.tellMessage(p, "party-introvert-inform");
		}
	}
}
