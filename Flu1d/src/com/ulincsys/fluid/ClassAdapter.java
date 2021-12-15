package com.ulincsys.fluid;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ClassAdapter {
	ClassInteractor C;
	
	public ClassAdapter(ClassInteractor C) {
		this.C = C;
	}
	
	public Object adapt(String object, String type, Function<InteractionContext, Boolean> onContext) throws ClassNotFoundException {
		Class<?> c = C.getClass(type);
		
		if(c == null) {
			FluidClassLoader loader = new FluidClassLoader(C);
			c = loader.loadUnknownClass(type);
		}
		
		return adapt(object, c, onContext);
	}
	
	public Object adapt(String object, Class<?> type, Function<InteractionContext, Boolean> onContext) {
		InteractionContext R = new InteractionContext();
		
		for(Constructor<?> c : type.getDeclaredConstructors()) {
			var params = c.getParameters();
			if(params.length == 1) {
				if(params[0].getType().isAssignableFrom(String.class)) {
					Annotation a = c.getDeclaredAnnotation(Deprecated.class);
					if(a != null && !onContext.apply(new InteractionContext().target(c).context(a.toString()).context(type)
							.context("Constructor<? assignable from String> for %s is marked for deprecation", c))) {
						break;
					} else if(!onContext.apply(new InteractionContext().target(c).context(type)
							.context("found Constructor<? assignable from String> for %s", c))) {
						break;
					}
					return C.instantiateClass(type, null, new Class<?>[] { params[0].getType() }, new Object[] { object })
					.onAnyContext(context -> {
						throw context;
					}, Context.FAILURE, Context.EXCEPTION).getTarget();
				}
			}
		}

		for(Method m : type.getDeclaredMethods()) {
			var params = m.getParameters();
			if(params.length == 1 && m.getName().startsWith("parse")) {
				if(params[0].getType().isAssignableFrom(String.class)) {
					Annotation a = m.getDeclaredAnnotation(Deprecated.class);
					if(a != null && !onContext.apply(new InteractionContext().target(m).context(a.toString()).context(type)
							.context("parseMethod(? assignable from String) for %s is marked for deprecation", m))) {
						break;
					} else if(!onContext.apply(new InteractionContext().target(m).context(type)
							.context("found parseMethod(? assignable from String) for %s", m))) {
						break;
					}
					return C.callDeclaredMethod(m, null, new Object[] { object }, context -> {
						return false;
					}).onAnyContext(context -> {
						throw context;
					}, Context.FAILURE, Context.EXCEPTION).getTarget();
				}
			}
		}
		
		throw R.context("Error: %s not adaptable from %s", type.getName(), object);
	}
}






































