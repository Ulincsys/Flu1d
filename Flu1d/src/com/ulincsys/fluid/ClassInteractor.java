package com.ulincsys.fluid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class ClassInteractor {
	public Map<String, Object> heap;
	public Map<String, Class<?>> classes;
	public ArrayList<String> classPath;
	public ArrayList<Object> results;
	
	private JavaCompiler compiler;
	private ClassAdapter adapter;
	private String[] defaultClassPath = { "java.lang", "java.util", "java.math" };
	private File compilationDir;
	
	public ClassInteractor(Class<?>... defaultClassPath) throws InteractionContext {
		this("compiledClasses", defaultClassPath);
	}
	
	public ClassInteractor(String cmpDir, Class<?>... defaultClassPath) throws InteractionContext {
		heap = new HashMap<String, Object>();
		classes = new HashMap<String, Class<?>>();
		results = new ArrayList<Object>();
		classPath = new ArrayList<String>();
		
		adapter = new ClassAdapter(this);
		compiler = ToolProvider.getSystemJavaCompiler();
		compilationDir = new File(cmpDir);
		
		if(!compilationDir.exists() && !compilationDir.mkdir() || !compilationDir.canWrite()) {
			if(hasCompiler()) {
				throw new InteractionContext()
				.context("Could not create or access provided compilation output directory")
				.target(compilationDir);
			}
		}
		
		for(Class<?> c : defaultClassPath) {
			String Package = c.getPackageName();
			if(!classPath.contains(Package)) {
				classPath.add(c.getPackageName());
			}
		}
		
		for(String path : this.defaultClassPath) {
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
	
	private InteractionContext failure() {
		return new InteractionContext(null, false);
	}
	
	private InteractionContext failure(String format, Object... args) {
		return new InteractionContext(String.format(format, args), false);
	}
	
	// ----------------------------------------------------------------------------- MANAGING
	
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
	
	public Class<?> undefineClass(String forName) {
		return classes.remove(forName);
	}

	public Object getVar(String forName) {
		return heap.get(forName);
	}
	
	public Object undefineVar(String forName) {
		return heap.remove(forName);
	}
	
	// ----------------------------------------------------------------------------- INVOCATION
	
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
	
	public InteractionContext callDeclaredMethod(Class<?> c, Object o,
			String var, String name, Class<?>[] params, Object[] args) {
		
		Method m;
		try {
			m = c.getDeclaredMethod(name, params);
		} catch (NoSuchMethodException | SecurityException e) {
			return failure("Error calling method %s on %s", name,
					name != null ? name : c.getName()).context(e);
		}
		
		return callDeclaredMethod(m, o, args, context -> {
			return true;
		});
	}
	
	public InteractionContext callDeclaredMethod(Method m, Object o, Object[] args, Function<InteractionContext, Boolean> onContext) {
		InteractionContext R = failure();
		
		try {
			Object result = m.invoke(o, args);
			if(m.getReturnType() == void.class) {
				R.context("Method execution concluded");
			} else if(result == null) {
				R.context("Method returned null");
			} else {
				R.context("Method returned result").target(result);
				if(onContext.apply(R)) {
					results.add(result);
				}
			}
			return R.context(true);
		} catch (InvocationTargetException e) {
			return R.context("An exception occurred during execution of method %s", m.getName())
					.context(e);
		} catch(Exception e) {
			return R.context("Error calling method %s", m.getName()).context(e);
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

		return instantiateClass(c, var, params, args);
	}
	
	public InteractionContext instantiateClass(Class<?> c, String var, Class<?>[] params, Object[] args) {
		try {
			var R = success().context(c);
			Constructor<?> constructor = c.getDeclaredConstructor(params);
			Object o = constructor.newInstance(args);
			if(var != null) {
				Object prev = heap.put(var, o);
				if(prev != null) {
					R.context("Redefining %s as %s", var, c.getName())
					.previous(prev);
				}
			}
			return R.target(o);
		} catch(Exception e) {
			return failure("Error instantiating class %s", c.getName()).context(e).context(c);
		}
	}
	
	// ----------------------------------------------------------------------------- COMPILATION
	
	public InteractionContext compileClass(String inputFile) {
		OutputStream out = new ByteArrayOutputStream();
		try {
			return compileClass(null, out, out, "-d", compilationDir.getCanonicalPath(), inputFile);
		} catch (IOException e) {
			return failure("An exception occurred while referencing the compilation directory")
					.context(e);
		}
	}
	
	public InteractionContext compileClass(InputStream in, OutputStream out, OutputStream err, String... args) {
		if(!hasCompiler()) {
			return failure("Compilation is not available on this platform")
					.context("Platform returned null when querying for available compiler, is a JDK installed?");
		}
		
		try {
			return new InteractionContext().target(compiler.run(in, out, err, args))
					.context("Compilation completed")
					.context(out instanceof ByteArrayOutputStream ?
							ByteArrayOutputStream.class.cast(out).toString() :
							"Output in provided OutputStream")
					.onTarget((context, target) -> {
						Integer result = Integer.class.cast(target);
						context.context(result == 0 ? true : false);
					});
		} catch(Exception e) {
			return failure("An exception occurred during compilation")
					.context(e);
		}
	}
	
	// ----------------------------------------------------------------------------- GETTING AND SETTING

	public ClassAdapter getAdapter() {
		return adapter;
	}
	
	public Boolean hasCompiler() {
		return compiler != null;
	}
}































