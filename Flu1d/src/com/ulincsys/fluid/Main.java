package com.ulincsys.fluid;

public class Main {
	public static Console console;
	public static ClassInteractor C;
	
	public static void main(String[] args) {
		console = new Console();
		C = new ClassInteractor();
		CommandHandler.console = console;
		CommandHandler.C = C;
		
		console.start();
		try {
			console.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


































