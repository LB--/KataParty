package com.lb_stuff.kataparty.api;

import com.lb_stuff.kataparty.api.IParty.IMember;

import org.bukkit.command.CommandSender;

/**
 * Permission checking for {@link CommandSender}s and {@link IMember}s.
 */
public final class Perms
{
	private Perms()
	{
		throw new UnsupportedOperationException();
	}

	private static boolean P(CommandSender s, String p)
	{
		return s != null && s.hasPermission("KataParty."+p);
	}
	private static boolean M(IMember m)
	{
		return m != null && (m.getRank() == PartyRank.MODERATOR || m.getRank() == PartyRank.ADMIN);
	}
	private static boolean A(IMember m)
	{
		return m != null && m.getRank() == PartyRank.ADMIN;
	}

	public static boolean pluginInfo       (CommandSender s          ){ return P(s, "info"); }
	public static boolean chatToggle       (CommandSender s          ){ return P(s, "toggle"); }
	public static boolean create           (CommandSender s          ){ return P(s, "create"); }
	public static boolean visibilityToggle (CommandSender s          ){ return P(s, "hide"); }
	public static boolean visibilityToggle (IMember m                ){ return A(m); }
	public static boolean listHidden       (CommandSender s          ){ return P(s, "seehidden"); }
	public static boolean stickyToggle     (CommandSender s          ){ return P(s, "stick"); }
	public static boolean stickyToggle     (IMember m                ){ return A(m); }
	public static boolean list             (CommandSender s          ){ return P(s, "list"); }
	public static boolean join             (CommandSender s          ){ return P(s, "join"); }
	public static boolean disband          (CommandSender s          ){ return P(s, "disband"); }
	public static boolean disband          (IMember m                ){ return A(m); }
	public static boolean manage           (CommandSender s          ){ return P(s, "manage"); }
	public static boolean rename           (IMember m                ){ return A(m); }
	public static boolean arbiter          (CommandSender s          ){ return P(s, "arbiter"); }
	public static boolean tpGoto           (CommandSender s          ){ return P(s, "teleport.do"); }
	public static boolean tpSummon         (IMember m                ){ return A(m); }
	public static boolean tpPref           (CommandSender s          ){ return P(s, "teleport.disallow"); }
	public static boolean tpToggle         (CommandSender s          ){ return P(s, "teleport.disable"); }
	public static boolean tpToggle         (IMember m                ){ return M(m); }
	public static boolean pvpToggle        (IMember m                ){ return M(m); }
	public static boolean inventoryView    (CommandSender s          ){ return P(s, "inventory.use"); }
	public static boolean inventoryToggle  (CommandSender s          ){ return P(s, "inventory.enable"); }
	public static boolean inventoryToggle  (IMember m                ){ return M(m); }
	public static boolean inviteToggle     (CommandSender s          ){ return P(s, "invite.enforce"); }
	public static boolean inviteCreate     (CommandSender s          ){ return P(s, "invite.create"); }
	public static boolean inviteCreate     (IMember m                ){ return A(m); }
	public static boolean inviteAccept     (CommandSender s          ){ return P(s, "invite.accept"); }
	public static boolean configReload     (CommandSender s          ){ return P(s, "reload"); }
	public static boolean updateInform     (CommandSender s          ){ return P(s, "update-notify"); }
	public static boolean healthToggle     (CommandSender s          ){ return P(s, "shared-health.toggle"); }
	public static boolean healthPartake    (CommandSender s          ){ return P(s, "shared-health.contribute"); }
	public static boolean xpContribute     (CommandSender s          ){ return P(s, "shared-xp.contribute"); }
	public static boolean xpBenefit        (CommandSender s          ){ return P(s, "shared-xp.benefit"); }
	public static boolean potionsContribute(CommandSender s          ){ return P(s, "smart-splash-potions.contribute"); }
	public static boolean potionsBenefit   (CommandSender s          ){ return P(s, "smart-splash-potions.benefit"); }
	public static boolean rejoin           (CommandSender s          ){ return P(s, "back.use"); }
	public static boolean rejoinPeriod     (CommandSender s, String g){ return P(s, "back.grace-periods."+g); }
	public static boolean pardon           (CommandSender s          ){ return P(s, "pardon"); }
	public static boolean membersScoreboard(CommandSender s          ){ return P(s, "scoreboard"); }
}
