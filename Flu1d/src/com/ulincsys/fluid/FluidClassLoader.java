package com.ulincsys.fluid;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

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
	
	public Class<?> loadClass(File file, String forName) throws IOException, ClassNotFoundException {
		URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, this);
		try {
			return loader.loadClass(forName);
		} finally {
			loader.close();
		}
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



































