package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IMetadatable;
import com.lb_stuff.kataparty.api.event.MetadataAttachEvent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metadatable implements IMetadatable
{
	public static class EntrySerializer implements ConfigurationSerializable
	{
		@Override
		public Map<String, Object> serialize()
		{
			Map<String, Object> data = new HashMap<>();
			data.put("c", c.getName());
			data.put("m", m);
			return data;
		}
		public EntrySerializer(Map<String, Object> data) throws ClassNotFoundException
		{
			Class<? extends ConfigurationSerializable> temp = KataPartyPlugin.SerializableDummy.class;
			try
			{
				temp = (Class<? extends ConfigurationSerializable>)Class.forName((String)data.get("c"));
			}
			catch(ClassNotFoundException e)
			{
			}
			c = temp;
			m = (ConfigurationSerializable)data.get("m");
		}

		public final Class<? extends ConfigurationSerializable> c;
		public final ConfigurationSerializable m;
		public EntrySerializer(Class<? extends ConfigurationSerializable> clazz, ConfigurationSerializable meta)
		{
			c = clazz;
			m = meta;
		}
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<>();
		List<EntrySerializer> entries = new ArrayList<>();
		data.put("meta", entries);
		for(Map.Entry<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> e : meta.entrySet())
		{
			entries.add(new EntrySerializer(e.getKey(), e.getValue()));
		}
		return data;
	}
	public static Metadatable deserialize(Map<String, Object> data)
	{
		Metadatable m = new Metadatable();
		Object o = data.get("meta");
		if(o != null)
		{
			for(EntrySerializer e : (List<EntrySerializer>)o)
			{
				m.meta.put((Class<? extends ConfigurationSerializable>)e.c, e.m);
			}
		}
		return m;
	}

	public Metadatable()
	{
	}
	public Metadatable(IMetadatable other)
	{
		meta.putAll(other.getAll());
	}

	private final Map<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> meta = new HashMap<>();
	@Override
	public ConfigurationSerializable set(Class<? extends ConfigurationSerializable> clazz, ConfigurationSerializable v)
	{
		Bukkit.getPluginManager().callEvent(new MetadataAttachEvent(this, clazz, v));
		if(v == null)
		{
			return meta.remove(clazz);
		}
		return meta.put(clazz, v);
	}
	@Override
	public ConfigurationSerializable get(Class<? extends ConfigurationSerializable> clazz)
	{
		return meta.get(clazz);
	}
	@Override
	public Map<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> getAll()
	{
		Map<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> clone = new HashMap<>();
		clone.putAll(meta);
		return clone;
	}
	@Override
	public void setAll(IMetadatable m)
	{
		for(Map.Entry<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> e : m.getAll().entrySet())
		{
			set(e.getKey(), e.getValue());
		}
	}
	private static final MemoryConfiguration mc = new MemoryConfiguration();
	@Override
	public void cloneAll(IMetadatable m)
	{
		for(Map.Entry<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> e : m.getAll().entrySet())
		{
			mc.set("v", e.getValue());
			set(e.getKey(), (ConfigurationSerializable)mc.get("v"));
			mc.set("v", null);
		}
	}
}
