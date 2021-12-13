package com.ulincsys.fluid;

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
		return null;
	}
}






































