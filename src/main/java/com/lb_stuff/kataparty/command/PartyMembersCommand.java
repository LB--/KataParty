package com.lb_stuff.kataparty.command;

import com.lb_stuff.kataparty.KataPartyPlugin;
import com.lb_stuff.kataparty.api.IParty;
import com.lb_stuff.kataparty.api.event.PartyMemberJoinEvent;
import com.lb_stuff.kataparty.api.event.PartyMemberLeaveEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyMembersCommand extends PartyCommand implements Listener
{
	public PartyMembersCommand(KataPartyPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(args.length == 0)
			{
				IParty.IMember m = inst.getPartySet().findMember(player.getUniqueId());
				if(m == null)
				{
					inst.tellMessage(player, "not-in-party");
				}
				else
				{
					if(ScoreboardMeta.getFrom(player) == null)
					{
						ScoreboardMeta.addTo(player);
						startFor(player.getUniqueId());
					}
					else
					{
						ScoreboardMeta.removeFrom(player);
						stopFor(player.getUniqueId());
					}
				}
				return true;
			}
		}
		return false;
	}

	private final Map<UUID, ScoreboardTask> boards = new HashMap<>();
	private void startFor(UUID uuid)
	{
		stopFor(uuid);
		OfflinePlayer p = inst.getServer().getOfflinePlayer(uuid);
		if(p.isOnline())
		{
			boards.put(uuid, new ScoreboardTask(p.getPlayer()));
		}
	}
	private void stopFor(UUID uuid)
	{
		ScoreboardTask t = boards.get(uuid);
		if(t != null)
		{
			t.cancel();
		}
		boards.remove(uuid);
		ScoreboardMeta.removeFrom(Bukkit.getOfflinePlayer(uuid));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		if(ScoreboardMeta.getFrom(e.getPlayer()) != null)
		{
			startFor(e.getPlayer().getUniqueId());
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		stopFor(e.getPlayer().getUniqueId());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberJoin(PartyMemberJoinEvent e)
	{
		startFor(e.getApplicant().getUuid());
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMemberLeave(PartyMemberLeaveEvent e)
	{
		stopFor(e.getMember().getUuid());
	}

	private class ScoreboardTask implements Runnable
	{
		private final Player p;
		private final Scoreboard sb = inst.getServer().getScoreboardManager().getNewScoreboard();
		private final int id = inst.getServer().getScheduler().scheduleSyncRepeatingTask(inst, this, 0, 10);
		public ScoreboardTask(Player player)
		{
			p = player;
		}

		public void cancel()
		{
			inst.getServer().getScheduler().cancelTask(id);
			p.setScoreboard(inst.getServer().getScoreboardManager().getMainScoreboard());
			boards.remove(p.getUniqueId());
		}

		@Override
		public void run()
		{
			if(p == null || sb == null)
			{
				cancel();
				return;
			}
			IParty.IMember m = inst.getPartySet().findMember(p.getUniqueId());
			if(m == null || ScoreboardMeta.getFrom(p) == null)
			{
				cancel();
				return;
			}
			Team t = sb.getTeam("green");
			if(t == null)
			{
				t = sb.registerNewTeam("green");
			}
			for(IParty.IMember mem : m.getParty())
			{
				OfflinePlayer offp = inst.getServer().getOfflinePlayer(mem.getUuid());
				t.addPlayer(offp);
			}
			{ //health
				Objective o = sb.getObjective(DisplaySlot.BELOW_NAME);
				if(o == null)
				{
					o = sb.registerNewObjective("Health", "health");
				}
				o.setDisplayName(inst.getMessage("members-scoreboard-health"));
				o.setDisplaySlot(DisplaySlot.BELOW_NAME);
				for(IParty.IMember mem : m.getParty())
				{
					OfflinePlayer offp = inst.getServer().getOfflinePlayer(mem.getUuid());
					sb.resetScores(offp.getName());
				}
			}
			{ //distance
				Objective o = sb.getObjective(DisplaySlot.SIDEBAR);
				if(o == null)
				{
					o = sb.registerNewObjective("Distance", "dummy");
				}
				o.setDisplayName(inst.getMessage("members-scoreboard-distance"));
				o.setDisplaySlot(DisplaySlot.SIDEBAR);
				for(IParty.IMember mem : m.getParty())
				{
					OfflinePlayer offp = inst.getServer().getOfflinePlayer(mem.getUuid());
					Player onp = offp.getPlayer();
					Score s = o.getScore(offp.getName());
					s.setScore(-1);
					if(offp.isOnline() && p.canSee(onp) && p.getLocation().getWorld().equals(onp.getLocation().getWorld()))
					{
						s.setScore((int)p.getLocation().toVector().distance(onp.getLocation().toVector()));
					}
				}
			}
			{ //rank
				Objective o = sb.getObjective(DisplaySlot.PLAYER_LIST);
				if(o == null)
				{
					o = sb.registerNewObjective("Rank", "dummy");
				}
				o.setDisplayName(inst.getMessage("members-scoreboard-rank"));
				o.setDisplaySlot(DisplaySlot.PLAYER_LIST);
				for(Player onp : inst.getServer().getOnlinePlayers())
				{
					o.getScore(onp.getPlayerListName()).setScore(-1);
				}
				for(IParty.IMember mem : m.getParty())
				{
					OfflinePlayer offp = inst.getServer().getOfflinePlayer(mem.getUuid());
					if(!offp.isOnline())
					{
						continue;
					}
					Player onp = offp.getPlayer();
					Score s = o.getScore(onp.getPlayerListName());
					switch(mem.getRank())
					{
						case MEMBER:
						{
							s.setScore(0);
						} break;
						case MODERATOR:
						{
							s.setScore(1);
						} break;
						case ADMIN:
						{
							s.setScore(2);
						} break;
						default: break;
					}
				}
			}
			p.setScoreboard(sb);
		}
	}

	public static class ScoreboardMeta implements ConfigurationSerializable
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			return data;
		}
		public ScoreboardMeta(final Map<String, Object> data)
		{
		}

		public ScoreboardMeta()
		{
		}

		public static void addTo(OfflinePlayer p)
		{
			KataPartyPlugin.getInst().getPlayerMetadata(p).set(ScoreboardMeta.class, new ScoreboardMeta());
		}
		public static ScoreboardMeta getFrom(OfflinePlayer p)
		{
			return (ScoreboardMeta)KataPartyPlugin.getInst().getPlayerMetadata(p).get(ScoreboardMeta.class);
		}
		public static void removeFrom(OfflinePlayer p)
		{
			KataPartyPlugin.getInst().getPlayerMetadata(p).set(ScoreboardMeta.class, null);
		}
	}
}
