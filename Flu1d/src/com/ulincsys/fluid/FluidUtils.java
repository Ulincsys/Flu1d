package com.ulincsys.fluid;

public class FluidUtils {
	private FluidUtils() {
		
	}
	
	static Class<?>[] toTypeArray(Object... objects) {
		Class<?>[] types = new Class<?>[objects.length];
		
		for(int i = 0; i < objects.length; ++i) {
			types[i] = objects[i].getClass();
		}
		
		return types;
	}
}
