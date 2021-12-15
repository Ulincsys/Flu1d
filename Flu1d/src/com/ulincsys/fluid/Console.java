package com.ulincsys.fluid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Console extends Thread {
	InputStream in;
	PrintStream out;
	
	BufferedReader reader;
	
	private String prompt = "Flu1d:~$ ";
	
	public Console() {
		this(System.in, System.out);
	}
	
	public Console(InputStream in, PrintStream out) {
		this.in = in;
		this.out = out;
	}
	
	@Override
	public void run() {
		reader = new BufferedReader(new InputStreamReader(in));
		try {
			do {
				out.print(prompt);
			} while(CommandHandler.execute(input()) != Commands.EXIT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String input() throws IOException {
		return reader.readLine();
	}
	
	public String tryInput() {
		try {
			return input();
		} catch(IOException e) {
			return "";
		}
	}
	
	public void log(String message) {
		log(message, null);
	}
	
	public void log(String message, String source) {
		if(source == null) {
			formatln("%s", message);			
		} else {
			formatln("[%s] %s", source, message);	
		}
	}
	
	public void formatln(String message, Object... args) {
		format(message + "\n", args);
	}
	
	public void format(String message, Object... args) {
		out.format(message, args);
	}
	
	public void printStackTrace(StackTraceElement[] message) {
		for(StackTraceElement element : message) {
			log(element.toString());
		}
	}
	
	public void logException(Throwable e) {
		format("Exception %s: %s\n", e.getClass().getName(), e.getMessage());
		printStackTrace(e.getStackTrace());
		while(e.getCause() != null) {
			log("Caused by:");
			e = e.getCause();
			format("Exception %s: %s\n", e.getClass().getName(), e.getMessage());
			printStackTrace(e.getStackTrace());
		}
	}
}






























