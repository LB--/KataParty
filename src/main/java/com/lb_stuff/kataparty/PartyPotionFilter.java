package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IParty;
import static com.lb_stuff.service.PotionFilterService.Splash;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public class PartyPotionFilter implements Listener
{
	private final KataPartyPlugin inst;
	public PartyPotionFilter(KataPartyPlugin plugin)
	{
		inst = plugin;
	}

	public static enum PotionDiplomacy
	{
		ABSORPTION       (PotionEffectType.ABSORPTION,        true ),
		BLINDNESS        (PotionEffectType.BLINDNESS,         false),
		CONFUSION        (PotionEffectType.CONFUSION,         false),
		DAMAGE_RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE, true ),
		FAST_DIGGING     (PotionEffectType.FAST_DIGGING,      true ),
		FIRE_RESISTANCE  (PotionEffectType.FIRE_RESISTANCE,   true ),
		HARM             (PotionEffectType.HARM,              false),
		HEAL             (PotionEffectType.HEAL,              true ),
		HEALTH_BOOST     (PotionEffectType.HEALTH_BOOST,      true ),
		HUNGER           (PotionEffectType.HUNGER,            false),
		INCREASE_DAMAGE  (PotionEffectType.INCREASE_DAMAGE,   true ),
		INVISIBILITY     (PotionEffectType.INVISIBILITY,      true ),
		JUMP             (PotionEffectType.JUMP,              true ),
		NIGHT_VISION     (PotionEffectType.NIGHT_VISION,      true ),
		POISON           (PotionEffectType.POISON,            false),
		REGENERATION     (PotionEffectType.REGENERATION,      true ),
		SATURATION       (PotionEffectType.SATURATION,        true ),
		SLOW             (PotionEffectType.SLOW,              false),
		SLOW_DIGGING     (PotionEffectType.SLOW_DIGGING,      false),
		SPEED            (PotionEffectType.SPEED,             true ),
		WATER_BREATHING  (PotionEffectType.WATER_BREATHING,   true ),
		WEAKNESS         (PotionEffectType.WEAKNESS,          false),
		WITHER           (PotionEffectType.WITHER,            false);
		private final PotionEffectType type;
		private final boolean beneficial;
		private PotionDiplomacy(PotionEffectType t, boolean b)
		{
			type = t;
			beneficial = b;
		}

		public PotionEffectType getType()
		{
			return type;
		}
		public boolean isBeneficial()
		{
			return beneficial;
		}

		public static PotionDiplomacy find(PotionEffectType t)
		{
			for(PotionDiplomacy pd : PotionDiplomacy.values())
			{
				if(pd.type.equals(t))
				{
					return pd;
				}
			}
			return null;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSplash(Splash s)
	{
		if(!inst.getConfig().getBoolean("smart-splash-potions"))
		{
			return;
		}
		ProjectileSource ps = s.getSource();
		LivingEntity le = s.getTarget();
		if(ps instanceof Player && le instanceof Player)
		{
			IParty.IMember source = inst.getPartySet().findMember(((Player)ps).getUniqueId());
			IParty.IMember target = inst.getPartySet().findMember(((Player)le).getUniqueId());
			if(source != null && target != null)
			{
				for(PotionEffect pe : s.getEffects())
				{
					PotionDiplomacy pd = PotionDiplomacy.find(pe.getType());
					if(pd == null)
					{
						inst.getLogger().warning("Unknown potion type \""+pe.getType().getName()+"\" - nag LB");
						continue;
					}
					if(source.getParty() == target.getParty())
					{
						if(!source.getParty().canPvp() && !pd.isBeneficial())
						{
							s.setIntensity(pe, 0.0);
						}
					}
					else
					{
						if(pd.isBeneficial())
						{
							s.setIntensity(pe, 0.0);
						}
					}
				}
			}
		}
	}
}
