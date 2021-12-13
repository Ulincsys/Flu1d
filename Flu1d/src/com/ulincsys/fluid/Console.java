package com.ulincsys.fluid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Console extends Thread {
	InputStream in;
	PrintStream out;
	
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
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		try {
			do {
				out.print(prompt);
			} while(CommandHandler.execute(r.readLine()) != Commands.EXIT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public void logException(Exception e) {
		format("Exception %s: %s", e.getClass().getName(), e.getMessage());
		log(e.getMessage());
		printStackTrace(e.getStackTrace());
	}
}






























