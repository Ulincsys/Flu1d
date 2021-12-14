package com.ulincsys.fluid;

import java.lang.reflect.Constructor;

public class ClassAdapter {
	ClassInteractor C;
	
	public ClassAdapter(ClassInteractor C) {
		this.C = C;
	}
	
	public Object adapt(String object, String type) throws ClassNotFoundException {
		Class<?> c = C.getClass(type);
		
		if(c == null) {
			FluidClassLoader loader = new FluidClassLoader(C);
			c = loader.loadUnknownClass(type);
		}
		
		return adapt(object, c);
	}
	
	public Object adapt(String object, Class<?> type) {
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		
		InteractionContext R = new InteractionContext();
		
		for(Constructor<?> c : constructors) {
			var params = c.getParameters();
			if(params.length == 1) {
				if(params[0].getType().isAssignableFrom(String.class)) {
					R = C.instantiateClass(type, null, new Class<?>[] { params[0].getType() }, new Object[] { object });
					if(R.hasExceptionContext()) {
						throw R;
					} else if(R.isSuccess()) {
						return C.undefineVar(null);
					}
				}
			}
		}
		
		throw R.context("Error: %s not adaptable from %s", type.getName(), object);
	}
}






































