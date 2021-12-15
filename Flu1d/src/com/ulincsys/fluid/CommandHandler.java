package com.ulincsys.fluid;

import java.util.ArrayList;
import java.util.function.BiFunction;

enum Commands {
	EXIT, IMPORT, NEW, CALL, HEAP, CLASSES, RESULTS, RELOAD, ADAPT, NOP;
}

public class CommandHandler {
	public static Console console;
	public static ClassInteractor C;
	
	public static Commands execute(String command) {
		String[] args = command.split(" ");

		try {
			switch(Commands.valueOf(args[0].toUpperCase())) {
			case IMPORT:
				if(args.length < 2) {
					console.log("Usage: import <className> [as <alias>]");
				} else {
					reflectiveImport(args);
				}
				return Commands.IMPORT;
			case NEW:
				if(args.length < 3) {
					console.log("Usage: new <className> <varName> [args...]");
				} else {
					reflectiveInstantiate(args[1], args[2], args);
				}
				return Commands.NEW;
			case CALL:
				if(args.length < 3) {
					console.log("Usage: call <classOrVarName> <methodName> [args...]");
				} else {
					reflectiveCall(args[1], args[2], args);
				}
				return Commands.CALL;
			case HEAP:
				listHeap();
				return Commands.HEAP;
			case CLASSES:
				listClasses();
				return Commands.CLASSES;
			case RESULTS:
				listResults();
				return Commands.RESULTS;
			case RELOAD:
				C = new ClassInteractor();
				return Commands.RELOAD;
			case ADAPT:
				if(args.length < 3) {
					console.log("Usage: adapt <className> <value>");
				} else {
					adaptClass(args[1], args[2]);
				}
				return Commands.ADAPT;
			case EXIT:
				return Commands.EXIT;
			default:
				break;
			}
		} catch(Exception e) {

		}
		return Commands.NOP;
	}
	
	private static void reflectiveImport(String[] args) {
		FluidClassLoader loader = new FluidClassLoader(C);
		String classPath = args[1];
		String alias = args.length == 4 ? args[3] : args[1];
		try {
			C.injectClass(loader.loadUnknownClass(classPath), alias)
			.onMessage(message -> {
				console.log(message);
			}).onTarget(target -> {
				console.formatln("Redefining alias from %s", target.toString());
			});
		} catch(Exception e) {
			console.log("An exception occurred while loading class:");
			console.logException(e);
		}
	}
	
	private static void adaptClass(String forName, String object) {
		try {
			Object o = C.getAdapter().adapt(object, forName, context -> {
				console.log(context.getMessage());
				console.format("Would you like to adapt with this method? [Y/n]:");
				if(console.tryInput().trim().toLowerCase().startsWith("n")) {
					return false;
				}
				return true;
			});
			console.log(o.getClass().toString());
			console.log(String.valueOf(o));
		} catch(Exception e) {
			console.log("An exception occurred adapting class");
			console.logException(e);
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
				console.logException(e);
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
			C.instantiateClass(classPath, var, classes.toArray(new Class<?>[0]), objects.toArray())
			.onMessage(message -> {
				console.log(message);
			}).onException(e -> {
				console.logException(e);
			});
		}
	}
	
	private static void reflectiveCall(String classOrVar, String method, String[] args) {
		ArrayList<Object> objects = new ArrayList<Object>();
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		Boolean isClass = C.getClass(classOrVar) != null;
		
		if(parseArgs(args, objects, classes)) {
			InteractionContext result;
			if(isClass) {
				result = C.callStaticMethod(classOrVar, method, classes.toArray(new Class<?>[0]), objects.toArray());
			} else {
				result = C.callMethod(classOrVar, method, classes.toArray(new Class<?>[0]), objects.toArray());
			}
			
			result.onMessage(message -> {
				console.log(message);
			}).onException(e -> {
				console.logException(e);
			});
		}
	}
	
	private static void listHeap() {
		console.log("Defined vars:");
		C.heap.entrySet().forEach(entry -> {
			console.formatln("%s: %s", entry.getKey(), entry.getValue().getClass().getName());
		});
	}
	
	private static void listClasses() {
		console.log("Imported classes:");
		C.classes.entrySet().forEach(entry -> {
			console.formatln("%s: %s", entry.getKey(), entry.getValue().getName());
		});
	}
	
	private static void listResults() {
		console.log("Accepted results:");
		C.results.forEach(result -> {
			console.formatln("%s: %s", result, result.getClass());
		});
	}
}































