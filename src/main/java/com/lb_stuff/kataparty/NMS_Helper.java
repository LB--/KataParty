package com.lb_stuff.kataparty;

import org.bukkit.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class NMS_Helper
{
	private static final String NMS_PACKAGE_PREFIX = "net.minecraft.server.v";
	private final KataPartyPlugin inst;
	private final String packageprefix;
	public NMS_Helper(KataPartyPlugin plugin)
	{
		inst = plugin;
		String pname = null;
		for(Package p : Package.getPackages())
		{
			pname = p.getName();
			if(pname.startsWith(NMS_PACKAGE_PREFIX))
			{
				break;
			}
		}
		packageprefix = pname;
		if(packageprefix == null)
		{
			inst.getLogger().warning("Could not locate any NMS version");
		}
		else
		{
			inst.getLogger().info("Using NMS v"+packageprefix.substring(NMS_PACKAGE_PREFIX.length()));
		}
	}

	public boolean canUse()
	{
		return packageprefix != null;
	}

	private static Class<?> findClass(String name)
	{
		try
		{
			return Class.forName(name);
		}
		catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	private static Method findMethod(Class<?> start, String name, Class<?>... argtypes)
	{
		Method m = null;
		while(start.getSuperclass() != null)
		{
			try
			{
				m = start.getDeclaredMethod(name, argtypes);
				break;
			}
			catch(NoSuchMethodException e)
			{
				start = start.getSuperclass();
			}
		}
		return m;
	}
	private static Object invokeMethod(Method m, Object inst, Object... args)
	{
		try
		{
			m.setAccessible(true);
			return m.invoke(inst, args);
		}
		catch(IllegalAccessException|InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}
	private static Object invokeMethod(Object inst, String name, Class<?>[] argtypes, Object... args)
	{
		return invokeMethod(findMethod(inst.getClass(), name, argtypes), inst, args);
	}
	private static Field findField(Class<?> start, String name)
	{
		Field f = null;
		while(start.getSuperclass() != null)
		{
			try
			{
				f = start.getDeclaredField(name);
				break;
			}
			catch(NoSuchFieldException e)
			{
				start = start.getSuperclass();
			}
		}
		return f;
	}
	private static Object getField(Field f, Object inst)
	{
		try
		{
			f.setAccessible(true);
			return f.get(inst);
		}
		catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	private static Object getField(Object inst, String name)
	{
		return getField(findField(inst.getClass(), name), inst);
	}

	public Class<?> findNMS(String name)
	{
		return findClass(packageprefix+"."+name);
	}
	public static Object getHandle(Entity e)
	{
		return invokeMethod(e, "getHandle", new Class<?>[0]);
	}
	public Object getCombatTracker(Entity e)
	{
		final Object handle = getHandle(e);
		Method m = findMethod(handle.getClass(), "aW");
		if(m.getReturnType().equals(findNMS("CombatTracker")))
		{
			return invokeMethod(m, handle);
		}
		else
		{
			inst.getLogger().warning("NMS obfuscation changed! Nag LB about CombatTracker");
			return null;
		}
	}
	public void copyLastDamage(Entity from, Entity to)
	{
		final Object c1 = getCombatTracker(from);
		final Object c2 = getCombatTracker(to);

		if(c1 == null || c2 == null)
		{
			return;
		}

		Field f = findField(findNMS("CombatTracker"), "a");
		if(f.getType().equals(List.class))
		{
			List l1 = (List)getField(f, c1);
			List l2 = (List)getField(f, c2);
			if(l1.size() > 0)
			{
				l2.add(l1.get(l1.size()-1));
			}
		}
		else
		{
			inst.getLogger().warning("NMS obfuscation changed! Nag LB about CombatTracker's List");
		}
	}
}
