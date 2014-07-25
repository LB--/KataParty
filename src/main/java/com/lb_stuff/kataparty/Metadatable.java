package com.lb_stuff.kataparty;

import com.lb_stuff.kataparty.api.IMetadatable;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Map;
import java.util.HashMap;

public class Metadatable implements IMetadatable
{
	static
	{
		ConfigurationSerialization.registerClass(Metadatable.class);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<>();
		data.put("meta", meta);
		return data;
	}
	public static Metadatable deserialize(Map<String, Object> data)
	{
		Metadatable m = new Metadatable();
		Object o = data.get("meta");
		if(o != null)
		{
			m.meta.putAll((Map<Class<? extends ConfigurationSerializable>, ConfigurationSerializable>)o);
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
