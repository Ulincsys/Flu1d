package com.ulincsys.fluid;

public class FluidClassLoader extends ClassLoader {
	ClassInteractor C;
	
	public FluidClassLoader(ClassInteractor C) {
		super(C.getClass().getClassLoader());
		this.C = C;
	}
	
	@Override
	public Class<?> loadClass(String forName) throws ClassNotFoundException {
		return super.loadClass(forName);
	}
	
	public Class<?> loadSimpleClass(String forName) throws ClassNotFoundException {
		for(String path : C.classPath) {
			try {
				return loadClass(String.format("%s.%s", path, forName));
			} catch(Exception e) {
				continue; // just for fun
			}
		}
		
		throw new ClassNotFoundException("Class not found in path with simple name: " + forName);
	}
	
	public Class<?> loadUnknownClass(String forName) throws ClassNotFoundException {
		if(forName.indexOf('.') == -1) {
			return loadSimpleClass(forName);
		} else {
			return loadClass(forName);
		}
	}
}



































