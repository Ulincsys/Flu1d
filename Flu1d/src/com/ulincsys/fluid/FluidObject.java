package com.ulincsys.fluid;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a meta-object class which focuses on making reflection
 * easier to integrate into real workflows. 
 * 
 * @author ulincsys
 * @see ClassInteractor
 * @see FluidClassLoader
 * @see ClassAdapter
 * @see FluidUtils
 * @see InteractionContext
 */
public class FluidObject {
	Object o;
	Class<?> c;
	
	List<Method> methods;
	List<Constructor<?>> constructors;
	
	private static ClassInteractor interaction = new ClassInteractor();
	private static FluidClassLoader loader = new FluidClassLoader(interaction);
	
	private FluidObject() {

	}
	
	/**
	 * Creates an initialized {@link FluidObject} from the provided object.
	 * The runtime class of the object will be extracted and used to gain
	 * information about the methods and constructors available.
	 * 
	 * @see FluidClassLoader
	 * @param o The object to use for construction
	 * @throws IllegalArgumentException when the provided object is null.
	 * To construct a FluidObject without an instance, see {@link FluidObject#fromClass(Class)}
	 */
	public FluidObject(@NotNull Object o) throws IllegalArgumentException {
		if(o == null) {
			throw new IllegalArgumentException("FluidObject initial object must not be null. See FluidObject.fromClass()");
		}
		
		this.o = o;
		c = o.getClass();
		methods = Arrays.asList(c.getDeclaredMethods());
		constructors = Arrays.asList(c.getDeclaredConstructors());
	}
	
	/**
	 * Creates an uninitialized {@link FluidObject} from the provided class.
	 * 
	 * @param c The class with which to construct this FluidObject
	 */
	public static FluidObject fromClass(Class<?> c) {
		FluidObject f = new FluidObject();
		
		f.c = c;
		f.methods = Arrays.asList(c.getDeclaredMethods());
		f.constructors = Arrays.asList(c.getDeclaredConstructors());
		
		return f;
	}
	
	/**
	 * Creates an uninitialized {@link FluidObject} from the provided class.
	 * 
	 * @see FluidClassLoader
	 * @param forName A string class path representing the class to be used in
	 * construction of this FluidObject
	 * @throws ClassNotFoundException when the provided forName is not a valid
	 * class name
	 */
	public static FluidObject fromClass(String forName) throws ClassNotFoundException {
		Class<?> c = loader.loadClass(forName);
		
		return fromClass(c);
	}
	
	/**
	 * Takes an {@link Executable} object, and returns true if it is callable
	 * with the given list of class type parameters, else false.
	 * 
	 * @param e The executable object to query
	 * @param types The list of parameter types to use for instantiation
	 */
	public static Boolean isCallableWith(Executable e, Class<?>... types) {
		Class<?>[] parameters = e.getParameterTypes();
		
		if(parameters.length != types.length) {
			return false;
		}
		
		for(int i = 0; i < parameters.length; ++i) {
			if(!parameters[i].isAssignableFrom(types[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Calls the constructor of the class represented by this {@link FluidObject}
	 * with the given parameter list, if it exists. Will throw an exception
	 * if construction is not possible with the given parameters.
	 * 
	 * This operation will return the previous instantiation of the class, or null
	 * 
	 * @param parameters The list of parameters to use for instantiation
	 * @throws InteractionContext when an exception is encountered during instantiation
	 */
	public Object init(Object... parameters) throws InteractionContext {
		return interaction.instantiateClass(c, null, FluidUtils.toTypeArray(parameters), parameters)
				.onException((c, e) -> {
					throw c;
				}).onSuccess(c -> {
					c.previous(o);
					o = c.getTarget();
				}).getPrevious();
	}
	
	/**
	 * Returns true if the class represented by this {@link FluidObject}
	 * contains a constructor with the exact parameter list represented
	 * by the variable array of classes passed in the parameter list.
	 * 
	 * @param parameters The list of classes representing the parameter
	 * types of the constructor to search for
	 * @see FluidObject#isCallableWith
	 */
	public Boolean hasConstructor(Class<?>... parameters) {
		for(Constructor<?> c : constructors) {
			if(isCallableWith(c, parameters)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the class represented by this {@link FluidObject}
	 * contains at least one method with the given name, or false if it
	 * does not exist.
	 * 
	 * @param name The name of the methods to search for
	 */
	public Boolean hasMethod(String name) {
		return getMethodsWithName(name).size() > 0;
	}
	
	/**
	 * Returns true if the class represented by this {@link FluidObject}
	 * contains a method with the given name and type parameters,
	 * or false if it does not exist.
	 * 
	 * @param name The name of the methods to search for
	 * @param types The unique parameter list to narrow the method search 
	 */
	public Boolean hasMethod(String name, Class<?>... types) {
		return getMethod(name, types) != null;
	}
	
	/**
	 * Returns the method contained in the class represented by this
	 * {@link FluidObject} with the given name and type parameters,
	 * or null if it does not exist.
	 * 
	 * @param name The name of the methods to search for
	 * @param types The unique parameter list to narrow the method search 
	 * @see FluidObject#hasMethod
	 * @see Method
	 */
	public Method getMethod(String name, Class<?>... types) {
		for(Method m : getMethodsWithName(name)) {
			if(isCallableWith(m, types)) {
				return m;
			}
		}
		return null;
	}
	
	/**
	 * Counts the number of methods contained in the class represented
	 * by this {@link FluidObject} with the given name.
	 * 
	 * @param name The name of the methods to search for
	 * @see FluidObject#hasMethod
	 * @see Method
	 */
	public List<Method> getMethodsWithName(String name) {
		List<Method> namedMethods = new LinkedList<Method>();
		
		for(Method m : methods) {
			if(m.getName().equals(name)) {
				namedMethods.add(m);
			}
		}
		
		return namedMethods;
	}
	
	/**
	 * Returns true if this {@link FluidObject} contains a reference
	 * to a valid instance of an Object
	 */
	public Boolean isInitialized() {
		return o != null;
	}
}



























