package com.ulincsys.fluid;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInteractor {
	public Map<String, Object> heap;
	public Map<String, Class<?>> classes;
	public ArrayList<String> classPath;
	public ArrayList<Object> results;
	
	private ClassAdapter adapter;
	
	public ClassInteractor(Class<?>... defaultClassPath) {
		heap = new HashMap<String, Object>();
		classes = new HashMap<String, Class<?>>();
		results = new ArrayList<Object>();
		classPath = new ArrayList<String>();
		
		adapter = new ClassAdapter(this);
		
		for(Class<?> c : defaultClassPath) {
			String Package = c.getPackageName();
			if(!classPath.contains(Package)) {
				classPath.add(c.getPackageName());
			}
		}
		
		for(String path : new String[] { "java.lang", "java.util", "java.math" }) {
			if(!classPath.contains(path)) {
				classPath.add(path);
			}
		}
	}
	
	// ----------------------------------------------------------------------------- RETURNS
	
	private InteractionContext success() {
		return new InteractionContext(null, true);
	}
	
	@SuppressWarnings("unused")
	private InteractionContext success(String format, Object... args) {
		return new InteractionContext(String.format(format, args), true);
	}
	
	@SuppressWarnings("unused")
	private InteractionContext failure() {
		return new InteractionContext(null, false);
	}
	
	private InteractionContext failure(String format, Object... args) {
		return new InteractionContext(String.format(format, args), false);
	}
	
	// ----------------------------------------------------------------------------- INJECTING
	
	public InteractionContext injectClass(Class<?> c) {
		return injectClass(c, c.getSimpleName()).onMessage((context, message) -> {
			context.context("The class %s has no simple name available", c.getName());
		});
		
	}
	
	public InteractionContext injectClass(Class<?> c, String alias) {
		InteractionContext R = success();

		if(alias == null || alias.length() == 0) {
			alias = c.getName();
			R.context("Alias invalid, the fully qualified name will be used.", alias);
		}
		
		return success().context(c).target(classes.put(alias, c));
	}
	
	public Class<?> getClass(String forName) {
		return classes.get(forName);
	}

	public Object getVar(String forName) {
		return heap.get(forName);
	}
	
	// ----------------------------------------------------------------------------- CALLING
	
	public InteractionContext callMethod(String var, String name) {
		return callMethod(var, name, new Class<?>[0], new Object[0]);
	}
	
	public InteractionContext callMethod(String var, String name, Class<?>[] params, Object[] args) {
		Object o = getVar(var);
		if(o == null) {
			return failure("Error calling method %s on %s, "
					+ "that variable does not exist", name, var);
		}
		
		return callDeclaredMethod(o.getClass(), o, var, name, params, args);
	}
	
	public InteractionContext callStaticMethod(String forName, String name) {
		return callStaticMethod(forName, name, new Class<?>[0], new Object[0]);
	}
	
	public InteractionContext callStaticMethod(String forName, String name, Class<?>[] params, Object[] args) {
		Class<?> c = getClass(forName);
		if(c == null) {
			return failure("Error calling static method %s on %s, "
					+ "that class does not exist", name, forName);
		}
		
		return callDeclaredMethod(c, null, forName, name, params, args);
	}
	
	private InteractionContext callDeclaredMethod(Class<?> c, Object o,
			String var, String name, Class<?>[] params, Object[] args) {
		InteractionContext R = new InteractionContext()
				.context(c).context(false);
		
		try {
			Method m = c.getDeclaredMethod(name, params);
			Object result = m.invoke(o, args);
			if(m.getReturnType() == void.class) {
				R.context("Method execution concluded");
			} else if(result == null) {
				R.context("Method returned null");
			} else {
				results.add(result);
				R.context("Method returned result").target(result);
			}
			return R.context(true);
		} catch (InvocationTargetException e) {
			return R.context("An exception occurred during execution of method %s on %s", name, var)
					.context(e);
		} catch(Exception e) {
			return R.context("Error calling method %s on %s", name, var).context(e);
		}
	}
	
	// ----------------------------------------------------------------------------- INSTANTIATION
	
	public InteractionContext instantiateClass(String forName, String var) {
		return instantiateClass(forName, var, new Class<?>[0], new Object[0]);
	}
	
	public InteractionContext instantiateClass(String forName, String var, Class<?>[] params, Object[] args) {
		Class<?> c = getClass(forName);
		
		if(c == null) {
			return failure("Error instantiating class %s, no class found with that name", forName);
		}
		
		try {
			var R = success().context(c);
			Constructor<?> constructor = c.getDeclaredConstructor(params);
			Object o = constructor.newInstance(args);
			Object prev = heap.put(var, o);
			if(prev != null) {
				return R.context("Redefining %s as %s", var, forName);
			}
			return R;
		} catch(Exception e) {
			return failure("Error instantiating class %s", forName).context(e).context(c);
		}
	}
}































