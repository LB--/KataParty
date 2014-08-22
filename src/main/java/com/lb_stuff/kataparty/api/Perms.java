package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.api.IParty.IMember;

import org.bukkit.entity.Player;

/**
 * Permission checking for {@link Player}s and {@link IMember}s.
 */
public final class Perms
{
	private Perms()
	{
		throw new UnsupportedOperationException();
	}

	private static boolean P(Player p, String sn)
	{
		return p.hasPermission("KataParty."+sn);
	}

	public static boolean pluginInfo       (Player p          ){ return P(p, "info"); }
	public static boolean chatToggle       (Player p          ){ return P(p, "toggle"); }
	public static boolean partyCreate      (Player p          ){ return P(p, "create"); }
	public static boolean partyHide        (Player p          ){ return P(p, "hide"); }
	public static boolean partyListHidden  (Player p          ){ return P(p, "seehidden"); }
	public static boolean partySticky      (Player p          ){ return P(p, "stick"); }
	public static boolean partyList        (Player p          ){ return P(p, "list"); }
	public static boolean partyJoin        (Player p          ){ return P(p, "join"); }
	public static boolean partyDisband     (Player p          ){ return P(p, "disband"); }
	public static boolean partyManage      (Player p          ){ return P(p, "manage"); }
	public static boolean arbiter          (Player p          ){ return P(p, "admin"); }
	public static boolean teleportDo       (Player p          ){ return P(p, "teleport.do"); }
	public static boolean teleportPref     (Player p          ){ return P(p, "teleport.disallow"); }
	public static boolean teleportToggle   (Player p          ){ return P(p, "teleport.disable"); }
	public static boolean inventoryView    (Player p          ){ return P(p, "inventory.use"); }
	public static boolean inventoryToggle  (Player p          ){ return P(p, "inventory.enable"); }
	public static boolean inviteToggle     (Player p          ){ return P(p, "invite.enforce"); }
	public static boolean inviteCreate     (Player p          ){ return P(p, "invite.create"); }
	public static boolean inviteAccept     (Player p          ){ return P(p, "invite.accept"); }
	public static boolean configReload     (Player p          ){ return P(p, "reload"); }
	public static boolean updateInform     (Player p          ){ return P(p, "update-notify"); }
	public static boolean healthToggle     (Player p          ){ return P(p, "shared-health.toggle"); }
	public static boolean healthPartake    (Player p          ){ return P(p, "shared-health.contribute"); }
	public static boolean xpContribute     (Player p          ){ return P(p, "shared-xp.contribute"); }
	public static boolean xpBenefit        (Player p          ){ return P(p, "shared-xp.benefit"); }
	public static boolean potionsContribute(Player p          ){ return P(p, "smart-splash-potions.contribute"); }
	public static boolean potionsBenefit   (Player p          ){ return P(p, "smart-splash-potions.benefit"); }
	public static boolean partyRejoin      (Player p          ){ return P(p, "back.use"); }
	public static boolean partyRejoinPeriod(Player p, String g){ return P(p, "back.grace-periods."+g); }
	public static boolean partyPardon      (Player p          ){ return P(p, "pardon"); }
	public static boolean membersScoreboard(Player p          ){ return P(p, "scoreboard"); }
}
