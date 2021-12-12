package com.ulincsys.fluid;

public class ClassInteractorContext extends RuntimeException {
	private static final long serialVersionUID = -132421980258494686L;
	
	private Class<?> classContext;
	private Exception exceptionContext;
	private String message;
	private Boolean success;
	
	public ClassInteractorContext(String message) {
		this(null, null, message, null);
	}
	
	public ClassInteractorContext(String message, Boolean success) {
		this(null, null, message, success);
	}
	
	public ClassInteractorContext(String message, Class<?> c) {
		this(c, null, message, null);
	}
	
	public ClassInteractorContext(String message, Exception e) {
		this(null, e, message, null);
	}
	
	public ClassInteractorContext(Class<?> c, Exception e, String message, Boolean success) {
		classContext = c;
		exceptionContext = e;
		this.message = message;
		this.success = success;
	}
	
	public Class<?> getClassContext() {
		return classContext;
	}
	
	public Exception getExceptionContext() {
		return exceptionContext;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Boolean isSuccess() {
		return success;
	}
	
	public Boolean hasClassContext() {
		return classContext != null;
	}
	
	public Boolean hasExceptionContext() {
		return exceptionContext != null;
	}
	
	public Boolean hasSuccess() {
		return success != null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Message:\n")
		.append(message)
		.append("\nClass Context:\n")
		.append(classContext)
		.append("\nException Context:\n")
		.append(exceptionContext)
		.append("\nSuccess:\n")
		.append(success);
		
		return builder.toString();
	}
}






































