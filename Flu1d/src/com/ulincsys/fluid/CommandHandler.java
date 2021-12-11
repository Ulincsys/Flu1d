package com.ulincsys.fluid;

import java.util.ArrayList;
import java.util.function.BiFunction;

enum Commands {
	EXIT, IMPORT, NEW, CALL, HEAP, CLASSES, RESULTS, NOP;
}

public class CommandHandler {
	public static Console console;
	public static ClassInteractor C;
	
	public static Commands execute(String command) {
		String[] args = command.split(" ");

		try {
			switch(Commands.valueOf(args[0].toUpperCase())) {
			case IMPORT:
				if(args.length != 2) {
					console.log("Usage: import className");
				} else {
					reflectiveImport(args[1]);
				}
				return Commands.IMPORT;
			case NEW:
				if(args.length < 3) {
					console.log("Usage: new className varName [args...]");
				} else {
					reflectiveInstantiate(args[1], args[2], args);
				}
				return Commands.NEW;
			case CALL:
				if(args.length < 3) {
					console.log("Usage: call classOrVarName methodName [args...]");
				} else {
					reflectiveCall(args[1], args[2], args);
				}
				return Commands.CALL;
			case HEAP:
				C.listHeap();
				return Commands.HEAP;
			case CLASSES:
				C.listClasses();
				return Commands.CLASSES;
			case RESULTS:
				C.listResults();
				return Commands.RESULTS;
			case EXIT:
				return Commands.EXIT;
			default:
				break;
			}
		} catch(Exception e) {

		}
		return Commands.NOP;
	}
	
	private static void reflectiveImport(String classPath) {
		ClassLoader loader = Main.class.getClassLoader();
		
		try {
			Class<?> c = loader.loadClass(classPath);
			C.injectClass(c);
		} catch (ClassNotFoundException e) {
			console.formatln("Unable to load class %s", classPath);
		}
	}
	
	private static Boolean parseArgs(String[] args, ArrayList<Object> objects, ArrayList<Class<?>> classes) {
		BiFunction<String, String, Boolean> parser = (value, type) -> {
			try {
				if(type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")) {
					objects.add(Integer.parseInt(value));
					classes.add(Integer.class);
				} else if(type.equalsIgnoreCase("double")) {
					objects.add(Double.parseDouble(value));
					classes.add(Double.class);
				} else if(type.equalsIgnoreCase("string")) {
					objects.add(value);
					classes.add(String.class);
				} else if(type.equalsIgnoreCase("var")) {
					Object o = C.getVar(value);
					if(o == null) {
						console.formatln("Error parsing argument\n"
								+ "The variable %s was not found", value);
						return false;
					} else {
						objects.add(o);
						classes.add(o.getClass());
					}
				}
			} catch(Exception e) {
				console.formatln("Error parsing argument\n"
						+ "An exception occurred parsing %s as argument:", value);
				console.log(e.getMessage());
				return false;
			}
			return true;
		};
		
		for(int i = 3; i < args.length; ++i) {
			String[] split = args[i].split(":");
			if(split.length != 2) {
				console.formatln("Error parsing %s as an argument", args[i]);
				return false;
			}
			
			if(!parser.apply(split[0], split[1])) {
				return false;
			}
		}
		return true;
	}
	
	private static void reflectiveInstantiate(String classPath, String var, String[] args) {
		ArrayList<Object> objects = new ArrayList<Object>();
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		if(parseArgs(args, objects, classes)) {
			C.instantiateClass(classPath, var, classes.toArray(new Class<?>[0]), objects.toArray());
		}
	}
	
	private static void reflectiveCall(String classOrVar, String method, String[] args) {
		ArrayList<Object> objects = new ArrayList<Object>();
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		Boolean isClass = C.getClass(classOrVar) != null;
		
		if(parseArgs(args, objects, classes)) {
			if(isClass) {
				C.callStaticMethod(classOrVar, method, classes.toArray(new Class<?>[0]), objects.toArray());
			} else {
				C.callMethod(classOrVar, method, classes.toArray(new Class<?>[0]), objects.toArray());
			}
		}
	}
}






























