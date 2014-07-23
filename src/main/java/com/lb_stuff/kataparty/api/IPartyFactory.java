package com.lb_stuff.kataparty.api;

/**
 * Implementing classes are responsible for instantiating
 * {@link IParty} instances from {@link IPartySettings}
 * instances. This interface is not used during deserialization.
 */
public interface IPartyFactory
{
	/**
	 * Should create an {@link IParty} instance based on
	 * the {@link IPartySettings} instance, or else return
	 * <code>null</code>.
	 * @param parent The {@link IPartySet} in which the
	 * instance will be placed.
	 * @param from The {@link IPartySettings} from which to
	 * instantiate the {@link IParty} instance.
	 * @return The {@link IParty} instance.
	 */
	IParty create(IPartySet parent, IPartySettings from);

	/**
	 * Implementing classes are responsible for instantiating
	 * {@link IParty.IMember} instances from {@link IPartySettings.IMemberSettings}
	 * instances. This interface is not used during deserialization.
	 */
	public interface IMemberFactory
	{
		/**
		 * Should create an {@link IParty.IMember} instance based on
		 * the {@link IPartySettings.IMemberSettings} instance,
		 * or else return <code>null</code>.
		 * @param parent The {@link IParty} the returned
		 * {@link IParty.IMember} will be an instance of.
		 * @param from The {@link IPartySettings.IMemberSettings}
		 * from which to instantiate the {@link IParty.IMember}
		 * instance.
		 * @return The {@link IParty.IMember} instance.
		 */
		IParty.IMember create(IParty parent, IPartySettings.IMemberSettings from);
	}
}
