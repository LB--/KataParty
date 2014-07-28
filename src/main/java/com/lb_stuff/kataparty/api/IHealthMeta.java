package com.lb_stuff.kataparty.api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Used to track the shared health of an {@link IPartySettings}.
 * Intended to be an interface, but static interface methods are
 * not supported until Java 8.
 */
public abstract class IHealthMeta implements ConfigurationSerializable
{
	/**
	 * Returns the shared health as a value in the range [0.0, 1.0].
	 * @return The shared health as a value in the range [0.0, 1.0].
	 */
	public abstract double getPercent();
	/**
	 * Sets the shared health as a value in the range [0.0, 1.0].
	 * @param v The shared health as a value in the range [0.0, 1.0].
	 */
	public abstract void setPercent(double v);

	/**
	 * Retrieve an instance from an {@link IPartySettings}.
	 * @param m The {@link IPartySettings} to retrieve the
	 * instance from.
	 * @return The retrieved instance.
	 */
	public static IHealthMeta getFrom(IPartySettings m)
	{
		return (IHealthMeta)m.get(IHealthMeta.class);
	}
}
