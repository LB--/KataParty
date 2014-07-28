package com.lb_stuff.kataparty.api.event;

import com.lb_stuff.kataparty.api.IMetadatable;

import org.bukkit.event.HandlerList;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Event called when a {@link ConfigurationSerializable} is attached
 * to or removed from an {@link IMetadatable}. To retrieve the old
 * metadata, call {@link IMetadatable#get(java.lang.Class)} on the
 * value returned by {@link #getMetadatable()}.
 */
public final class MetadataAttachEvent extends KataPartyEvent
{
	private final IMetadatable metadatable;
	private final Class<? extends ConfigurationSerializable> key;
	private final ConfigurationSerializable meta;
	/**
	 * @param m The {@link IMetadatable}.
	 * @param clazz The {@link Class} with which the metadata is being attached.
	 * @param newmeta The new metadata. May be <code>null</code>.
	 */
	public MetadataAttachEvent(IMetadatable m, Class<? extends ConfigurationSerializable> clazz, ConfigurationSerializable newmeta)
	{
		if(m == null)
		{
			throw new IllegalArgumentException("Metadatable cannot be null");
		}
		if(clazz == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		metadatable = m;
		key = clazz;
		meta = newmeta;
	}

	/**
	 * Returns the {@link IMetadatable} to which the metadata is being attached.
	 * @return The {@link IMetadatable} to which the metadata is being attached.
	 */
	public IMetadatable getMetadatable()
	{
		return metadatable;
	}
	/**
	 * Returns the {@link Class} with which the metadata is being attached.
	 * @return The {@link Class} with which the metadata is being attached.
	 */
	public Class<? extends ConfigurationSerializable> getClassKey()
	{
		return key;
	}
	/**
	 * Returns the new metadata. May be <code>null</code>.
	 * @return The new metadata. May be <code>null</code>.
	 */
	public ConfigurationSerializable getMetadata()
	{
		return meta;
	}

	private static final HandlerList handlers = new HandlerList();
	/**
	 * See {@link org.bukkit.event.Event#getHandlers()}.
	 * @return The {@link org.bukkit.event.HandlerList}.
	 */
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	/**
	 * See {@link org.bukkit.event.Event#getHandlers()}.
	 * @return The {@link org.bukkit.event.HandlerList}.
	 */
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
