package com.ulincsys.fluid;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

enum Context {
	CLASS, EXCEPTION, MESSAGE, SUCCESS, FAILURE, TARGET;
}

public class InteractionContext extends RuntimeException {
	private static final long serialVersionUID = -132421980258494686L;
	
	private Class<?> classContext;
	private Exception exceptionContext;
	private StringBuilder message;
	private Boolean success;
	private Object target;
	
	public InteractionContext() {
		this(null, null, null, null);
	}
	
	public InteractionContext(String message) {
		this(message, null, null, null);
	}
	
	public InteractionContext(String message, Boolean success) {
		this(message, success, null, null);
	}
	
	public InteractionContext(String message, Class<?> c) {
		this(message, null, c, null);
	}
	
	public InteractionContext(String message, Exception e) {
		this(message, null, null, e);
	}
	
	public InteractionContext(String message, Boolean success, Class<?> c, Exception e) {
		classContext = c;
		exceptionContext = e;
		if(message != null) {
			this.message = new StringBuilder();
			this.message.append(message);
		}
		this.success = success;
	}
	
	public Class<?> getClassContext() {
		return classContext;
	}
	
	public Exception getExceptionContext() {
		return exceptionContext;
	}
	
	public String getMessage() {
		return message.toString();
	}

	public Object getTarget() {
		return target;
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
	
	public Boolean hasMessage() {
		return message != null;
	}
	
	public Boolean hasTarget() {
		return target != null;
	}
	
	public InteractionContext context(String message) {
		if(this.message == null) {
			this.message = new StringBuilder();
		} else {
			this.message.append('\n');
		}
		this.message.append(message);
		
		return this;
	}
	
	public InteractionContext context(String message, Object... args) {
		return context(String.format(message, args));
	}
	
	public InteractionContext context(Boolean success) {
		this.success = success;
		
		return this;
	}
	
	public InteractionContext context(Class<?> classContext) {
		this.classContext = classContext;
		
		return this;
	}
	
	public InteractionContext context(Exception exceptionContext) {
		this.exceptionContext = exceptionContext;
		
		return this;
	}
	
	public InteractionContext target(Object o) {
		target = o;
		
		return this;
	}
	
	private Object or(Object primary, Object secondary) {
		if(primary == null) {
			return secondary;
		}
		return primary;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Message:\n")
		.append(or(message, "None."))
		.append("\nClass Context:\n")
		.append(or(classContext, "None."))
		.append("\nException Context:\n")
		.append(or(exceptionContext, "None."))
		.append("\nSuccess:\n")
		.append(or(success, "None."));
		
		return builder.toString();
	}
	
	public Boolean hasContext(Context context) {
		switch(context) {
		case CLASS:
			return hasClassContext();
		case EXCEPTION:
			return hasExceptionContext();
		case FAILURE:
			return hasSuccess() && !isSuccess();
		case MESSAGE:
			return hasMessage();
		case SUCCESS:
			return hasSuccess() && isSuccess();
		case TARGET:
			return hasTarget();
		default:
			return false;
		}
	}
	
	/* This function accepts a consumer which takes a ClassInteractorContext,
	 * followed by a variable array of Context elements which determine under
	 * which contexts the consumer will be executed. The ClassInteractorContext
	 * passed to the consumer is the same as the one upon which the function
	 * call was made. The consumer will be executed at most once, if and only
	 * if at least one context in the provided array exists.
	 */
	public InteractionContext onAnyContext(Consumer<InteractionContext> consumer, Context... context) {
		for(Context c : context) {
			if(hasContext(c)) {
				consumer.accept(this);
				break;
			}
		}
		return this;
	}
	
	/* This function accepts a consumer which takes a ClassInteractorContext,
	 * followed by a variable array of Context elements which determine under
	 * which contexts the consumer will be executed. The ClassInteractorContext
	 * passed to the consumer is the same as the one upon which the function
	 * call was made. The consumer will be executed at most once, if and only
	 * if all contexts in the provided array exist.
	 */
	public InteractionContext onAllContexts(Consumer<InteractionContext> consumer, Context... context) {
		for(Context c : context) {
			if(!hasContext(c)) {
				return this;
			}
		}
		consumer.accept(this);
		return this;
	}
	
	public InteractionContext onMessage(BiConsumer<InteractionContext, String> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this, getMessage());
		}, Context.MESSAGE);
	}
	
	public InteractionContext onMessage(Consumer<String> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(getMessage());
		}, Context.MESSAGE);
	}
	
	public InteractionContext onClass(BiConsumer<InteractionContext, Class<?>> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this, getClassContext());
		}, Context.CLASS);
	}
	
	public InteractionContext onClass(Consumer<Class<?>> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(getClassContext());
		}, Context.CLASS);
	}
	
	public InteractionContext onSuccess(Consumer<InteractionContext> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this);
		}, Context.SUCCESS);
	}
	
	public InteractionContext onSuccess(Runnable runnable) {
		return onAnyContext(consume -> {
			runnable.run();
		}, Context.SUCCESS);
	}
	
	public InteractionContext onFailure(Consumer<InteractionContext> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this);
		}, Context.FAILURE);
	}
	
	public InteractionContext onFailure(Runnable runnable) {
		return onAnyContext(consume -> {
			runnable.run();
		}, Context.FAILURE);
	}
	
	public InteractionContext onException(BiConsumer<InteractionContext, Exception> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this, getExceptionContext());
		}, Context.EXCEPTION);
	}
	
	public InteractionContext onException(Consumer<Exception> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(getExceptionContext());
		}, Context.EXCEPTION);
	}
	
	public InteractionContext onTarget(BiConsumer<InteractionContext, Object> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this, getTarget());
		}, Context.TARGET);
	}
	
	public InteractionContext onTarget(Consumer<Object> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(getTarget());
		}, Context.TARGET);
	}
}






































