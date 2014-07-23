package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IPartySettings;
import static com.lb_stuff.kataparty.api.IPartySettings.IMemberSettings;
import com.lb_stuff.kataparty.api.PartyRank;

import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class PartySettings implements IPartySettings
{
	static
	{
		ConfigurationSerialization.registerClass(PartySettings.class);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<>();
		data.put("name", getName());
		data.put("tp", canTp());
		data.put("pvp", canPvp());
		data.put("visible", isVisible());
		data.put("inv", hasInventory());
		data.put("invite", isInviteOnly());
		data.put("sticky", isSticky());
		return data;
	}
	public static PartySettings deserialize(Map<String, Object> data)
	{
		PartySettings s = new PartySettings();
		s.name = (String)data.get("name");
		s.tp = (Boolean)data.get("tp");
		s.pvp = (Boolean)data.get("pvp");
		s.visible = (Boolean)data.get("visible");
		s.inv = (Boolean)data.get("inv");
		s.invite = (Boolean)data.get("invite");
		s.sticky = (Boolean)data.get("sticky");
		return s;
	}

	public PartySettings()
	{
	}
	public PartySettings(IPartySettings other)
	{
		name = other.getName();
		tp = other.canTp();
		pvp = other.canPvp();
		visible = other.isVisible();
		inv = other.hasInventory();
		invite = other.isInviteOnly();
		sticky = other.isSticky();
	}

	@Override
	public void apply(IPartySettings s)
	{
		setName(s.getName());
		setTp(s.canTp());
		setPvp(s.canPvp());
		setVisible(s.isVisible());
		setInventory(s.hasInventory());
		setInviteOnly(s.isInviteOnly());
		setSticky(s.isSticky());
	}

	private String name = "KataParty";
	@Override
	public String getName()
	{
		return name;
	}
	@Override
	public void setName(String n)
	{
		name = n;
	}

	private boolean tp = true;
	@Override
	public boolean canTp()
	{
		return tp;
	}
	@Override
	public void setTp(boolean enabled)
	{
		tp = enabled;
	}

	private boolean pvp = false;
	@Override
	public boolean canPvp()
	{
		return pvp;
	}
	@Override
	public void setPvp(boolean enabled)
	{
		pvp = enabled;
	}

	private boolean visible = true;
	@Override
	public boolean isVisible()
	{
		return visible;
	}
	@Override
	public void setVisible(boolean enabled)
	{
		visible = enabled;
	}

	private boolean inv = false;
	@Override
	public boolean hasInventory()
	{
		return inv;
	}
	@Override
	public void setInventory(boolean enabled)
	{
		inv = enabled;
	}

	private boolean invite = false;
	@Override
	public boolean isInviteOnly()
	{
		return invite;
	}
	@Override
	public void setInviteOnly(boolean enabled)
	{
		invite = enabled;
	}

	private boolean sticky = false;
	@Override
	public boolean isSticky()
	{
		return sticky;
	}
	@Override
	public void setSticky(boolean enabled)
	{
		sticky = enabled;
	}

	public static class MemberSettings implements IMemberSettings
	{
		static
		{
			ConfigurationSerialization.registerClass(MemberSettings.class);
		}

		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			data.put("tp", tp);
			data.put("rank", ""+rank);
			return data;
		}
		public static MemberSettings deserialize(Map<String, Object> data)
		{
			MemberSettings s = new MemberSettings(UUID.fromString((String)data.get("uuid")));
			s.tp = (Boolean)data.get("tp");
			s.rank = PartyRank.valueOf((String)data.get("rank"));
			return s;
		}

		public MemberSettings(UUID id)
		{
			uuid = id;
		}
		public MemberSettings(IMemberSettings other)
		{
			uuid = other.getUuid();
			tp = other.canTp();
			rank = other.getRank();
		}

		@Override
		public int hashCode()
		{
			return uuid.hashCode();
		}
		@Override
		public boolean equals(Object obj)
		{
			if(obj == null)
			{
				return false;
			}
			if(obj instanceof IMemberSettings)
			{
				return uuid.equals(((IMemberSettings)obj).getUuid());
			}
			if(obj instanceof UUID)
			{
				return uuid.equals((UUID)obj);
			}
			return false;
		}

		@Override
		public void apply(IMemberSettings s)
		{
			tp = s.canTp();
			rank = s.getRank();
		}

		private final UUID uuid;
		@Override
		public UUID getUuid()
		{
			return uuid;
		}

		private PartyRank rank = PartyRank.MEMBER;
		@Override
		public PartyRank getRank()
		{
			return rank;
		}
		@Override
		public void setRank(PartyRank r)
		{
			rank = r;
		}

		private boolean tp = KataPartyPlugin.getInst().getPartySet().defaultSelfTeleports();
		@Override
		public boolean canTp()
		{
			return tp;
		}
		@Override
		public void setTp(boolean enabled)
		{
			tp = enabled;
		}
	}
}
