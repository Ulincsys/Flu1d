package com.ulincsys.fluid;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClassInteractor {
	public static Console console;
	
	public Map<String, Object> heap;
	public Map<String, Class<?>> classes;
	
	public ArrayList<Object> results;
	
	public ClassInteractor() {
		heap = new HashMap<String, Object>();
		classes = new HashMap<String, Class<?>>();
		results = new ArrayList<Object>();
		
		console = Main.console;
	}
	
	public void injectClass(Class<?> c) {
		String name = c.getSimpleName();
		
		if(name.length() == 0) {
			name = c.getName();
			console.format("The class you have loaded has no simple name available"
					+ "\nThe fully qualified name will be used: %s", name);
		}
		
		classes.put(name, c);
	}
	
	public Class<?> getClass(String forName) {
		return classes.get(forName);
	}

	public Object getVar(String forName) {
		return heap.get(forName);
	}
	
	public Boolean callMethod(String var, String name) {
		return callMethod(var, name, new Class<?>[0], new Object[0]);
	}
	
	public Boolean callMethod(String var, String name, Class<?>[] params, Object[] args) {
		Object o = getVar(var);
		if(o == null) {
			console.formatln("Error calling method %s on %s\n"
					+ "That variable does not exist", name, var);
			return false;
		}
		
		return callDeclaredMethod(o.getClass(), o, var, name, params, args);
	}
	
	public Boolean callStaticMethod(String forName, String name) {
		return callStaticMethod(forName, name, new Class<?>[0], new Object[0]);
	}
	
	public Boolean callStaticMethod(String forName, String name, Class<?>[] params, Object[] args) {
		Class<?> c = getClass(forName);
		if(c == null) {
			console.formatln("Error calling static method %s on %s\n"
					+ "That class does not exist", name, forName);
			return false;
		}
		
		return callDeclaredMethod(c, null, forName, name, params, args);
	}
	
	private Boolean callDeclaredMethod(Class<?> c, Object o,
			String var, String name, Class<?>[] params, Object[] args) {
		
		try {
			Method m = c.getDeclaredMethod(name, params);
			Object result = m.invoke(o, args);
			Boolean isVoid = m.getReturnType() == void.class;
			console.format("Method execution concluded");
			if(isVoid) {
				console.log("");
			} else if(result == null) {
				console.log(", null returned");
			} else {
				console.log(", result accepted");
				results.add(result);
			}
			return true;
		} catch (NoSuchMethodException e) {
			console.formatln("Error calling method %s on %s\n"
					+ "That method does not exist", name, var);
		} catch (SecurityException e) {
			console.formatln("Error calling method %s on %s\n"
					+ "A Security Exception occurred:", name, var);
			console.log(e.getMessage());
		} catch (IllegalAccessException e) {
			console.formatln("Error calling method %s on %s\n"
					+ "That method is not accessible", name, var);
		} catch (IllegalArgumentException e) {
			console.formatln("Error calling method %s on %s\n"
					+ "One or more arguments do not match:", name, var);
			console.log(e.getMessage());
		} catch (InvocationTargetException e) {
			console.formatln("An exception occurred during execution of method %s on %s",
					name, var);
			console.log(e.getCause().getMessage());
		}
		return false;
	}
	
	public Boolean instantiateClass(String forName, String var) {
		return instantiateClass(forName, var, new Class<?>[0], new Object[0]);
	}
	
	public Boolean instantiateClass(String forName, String var, Class<?>[] params, Object[] args) {
		Class<?> c = getClass(forName);
		
		if(c == null) {
			console.formatln("Error instantiating class %s\nNo class loaded for that name", forName);
			return false;
		}
		
		try {
			Constructor<?> constructor = c.getDeclaredConstructor(params);
			Object o = constructor.newInstance(args);
			Object prev = heap.put(var, o);
			if(prev != null) {
				console.formatln("Redefining %s as %s", var, forName);
			}
		} catch(Exception e) {
			console.formatln("Error instantiating class: %s\n%s", forName, e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public void listHeap() {
		console.log("Defined vars:");
		heap.entrySet().forEach(entry -> {
			console.formatln("%s: %s", entry.getKey(), entry.getValue().getClass().getName());
		});
	}
	
	public void listClasses() {
		console.log("Imported classes:");
		classes.entrySet().forEach(entry -> {
			console.formatln("%s: %s", entry.getKey(), entry.getValue().getName());
		});
	}
	
	public void listResults() {
		console.log("Accepted results:");
		results.forEach(result -> {
			console.formatln("%s: %s", result, result.getClass());
		});
	}
}































