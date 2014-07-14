package com.lb_stuff.kataparty;

import static com.lb_stuff.kataparty.PartySet.MemberSettings;
import static com.lb_stuff.kataparty.ChatFilterPref.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import static org.bukkit.ChatColor.*;

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
	private String getSwap()
	{
		return inst.getFilterSwap();
	}

	@EventHandler(priority = EventPriority.HIGHEST) //highest executed last
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		if(!e.isCancelled())
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
			final String filtermsg = String.format(fmt, source.getDisplayName(), ""+GRAY+ITALIC+msg);

			//need to manually send to console since we are cancelling the event
			inst.getServer().getConsoleSender().sendMessage(normalmsg);

			for(Player target : targets)
			{
				String targetparty = null;
				ChatFilterPref targetpref = null;
				MemberSettings targetsettings = getSettings(target.getUniqueId());
				if(targetsettings != null)
				{
					targetparty = targetsettings.getPartyName();
					targetpref = targetsettings.getPref();

					if(sourcepref == null)
					{
						if(targetpref == null)
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
							target.sendMessage(""+BOLD+"{"+sourceparty+"}"+RESET+normalmsg);
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
							target.sendMessage(""+ITALIC+"{"+sourceparty+"}"+RESET+filtermsg);
						}
					}
				}

				//
			}

			e.setCancelled(true);
		}
	}
}
