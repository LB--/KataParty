package com.lb_stuff.kataparty.api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * Allows for persistent storage of metadata.
 */
public interface IMetadatable extends ConfigurationSerializable
{
	/**
	 * Replace a metadata with another. The types and return type may be disparate.
	 * @param clazz Key.
	 * @param v Value.
	 * @return Old value.
	 */
	ConfigurationSerializable set(Class<? extends ConfigurationSerializable> clazz, ConfigurationSerializable v);
	/**
	 * Get a metadata from a key. Key and value types may be disparate.
	 * @param clazz Key.
	 * @return Value.
	 */
	ConfigurationSerializable get(Class<? extends ConfigurationSerializable> clazz);
	/**
	 * Gets all metadata.
	 * @return All metadata.
	 */
	Map<Class<? extends ConfigurationSerializable>, ConfigurationSerializable> getAll();
	/**
	 * Sets all metadata from another {@link IMetadatable} instance.
	 * @param m The other {@link IMetadatable} instance.
	 */
	void setAll(IMetadatable m);
	/**
	 * Clones all metadata from another {@link IMetadatable} instance.
	 * @param m The other {@link IMetadatable} instance.
	 */
	void cloneAll(IMetadatable m);
}
