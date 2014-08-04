package com.lb_stuff.kataparty;

public final class PartyNametagManager implements OptionalComponent
{
	private final KataPartyPlugin inst;
	private OptionalComponent nametaglistener = null;
	public PartyNametagManager(KataPartyPlugin plugin)
	{
		inst = plugin;
		if(hasTagAPI())
		{
			nametaglistener = new PartyNametagListener(inst);
		}
	}

	private Boolean hastagapi = null;
	public boolean hasTagAPI()
	{
		if(hastagapi == null)
		{
			try
			{
				Class.forName("org.kitteh.tag.TagAPI");
				Class.forName("org.kitteh.tag.AsyncPlayerReceiveNameTagEvent");
				hastagapi = true;
			}
			catch(ClassNotFoundException e)
			{
				hastagapi = false;
			}
		}
		return hastagapi;
	}

	private boolean started = false;
	@Override
	public boolean isStarted()
	{
		return started;
	}
	@Override
	public void start()
	{
		if(isStarted())
		{
			return;
		}
		if(nametaglistener != null)
		{
			nametaglistener.start();
		}
		started = true;
	}
	@Override
	public void stop()
	{
		if(!isStarted())
		{
			return;
		}
		if(nametaglistener != null)
		{
			nametaglistener.stop();
		}
		started = false;
	}
}
