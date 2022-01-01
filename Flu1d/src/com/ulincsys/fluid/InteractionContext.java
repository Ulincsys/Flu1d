package com.ulincsys.fluid;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p> The core operating structure of the InteractionContext. </p>
 * 
 * <p> This enum represents every possible context state within
 * any given instance of InteractionContext. </p>
 * 
 * @see InteractionContext
 * @see #CLASS
 * @see #EXCEPTION
 * @see #MESSAGE
 * @see #SUCCESS
 * @see #FAILURE
 * @see #TARGET
 * @see #PREVIOUS
 * @author ulincsys
 */
enum Context {
	/**
	 * Represents that an InteractionContext holds a reference to, and interacted with a Class at one point during execution.
	 */
	CLASS,
	/**
	 * Represents that an exception occurred during execution within the originating context, and is referenced by this InteractionContext.
	 */
	EXCEPTION,
	/**
	 * Represents that at least one message was generated during execution within the originating context, and is referenced by this InteractionContext.
	 */
	MESSAGE,
	/**
	 * Indicates that this context represents a successful action.
	 */
	SUCCESS,
	/**
	 * Indicates that this context represents a failed action.
	 */
	FAILURE,
	/**
	 * Represents that this context was assigned a target (or newly generated object) during execution, and is referenced by this InteractionContext.
	 */
	TARGET,
	/**
	 * Represents that a previous value was overwritten in the originating context, and is referenced by this InteractionContext.
	 */
	PREVIOUS;
}

/**
 * This is a message passing class which communicates the state of any
 * given originating context, and also serves as an exception carrier
 * to bring exceptions from a lower context up to the calling context
 * without the need to catch a throwable. This class extends
 * RuntimeException, and so can be optionally thrown from any context.
 * 
 * @author ulincsys
 * @see Context
 */
public class InteractionContext extends RuntimeException {
	private static final long serialVersionUID = -132421980258494686L;
	
	private Class<?> classContext;
	private Exception exceptionContext;
	private StringBuilder message;
	private Boolean success;
	private Object target;
	private Object previous;
	
	/**
	 * Instantiates a context without context.
	 */
	public InteractionContext() {
		this(null, null, null, null);
	}
	
	/**
	 * Instantiates a context with a single message string.
	 * 
	 * @param message A string describing the context of this instance
	 * @see Context#MESSAGE
	 */
	public InteractionContext(String message) {
		this(message, null, null, null);
	}
	
	/**
	 * Instantiates a context with both a message and success or failure state.
	 * 
	 * @param message A string describing the context of this instance
	 * @param success A boolean defining the success state of this context
	 * @see Context#MESSAGE
	 * @see Context#SUCCESS
	 * @see Context#FAILURE
	 */
	public InteractionContext(String message, Boolean success) {
		this(message, success, null, null);
	}
	
	/**
	 * Instantiates a context with both a message and a relevant Class.
	 * 
	 * @param message A string describing the context of this instance
	 * @param c A Class relevant to this context
	 * @see Context#MESSAGE
	 * @see Context#CLASS
	 */
	public InteractionContext(String message, Class<?> c) {
		this(message, null, c, null);
	}
	
	/**
	 * Instantiates a context with both a message and a relevant Exception.
	 * @param message A string describing the context of this instance
	 * @param e An Exception relevant to this context
	 * @see Context#MESSAGE
	 * @see Context#EXCEPTION
	 */
	public InteractionContext(String message, Exception e) {
		this(message, null, null, e);
	}
	
	/**
	 * Instantiates a context with a message, a success state, a relevant Class,
	 * and a relevant Exception.
	 * 
	 * <p> Note that not all context states are reachable with this constructor,
	 * and additional contexts, such as {@link Context#TARGET} and
	 * {@link Context#PREVIOUS}, must be set manually using {@link #target(Object)}
	 * or {@link #previous(Object)} </p>
	 * @param message A string describing the context of this instance
	 * @param success A boolean defining the success state of this context
	 * @param c A Class relevant to this context
	 * @param e An Exception relevant to this context
	 * 
	 * @see Context
	 */
	public InteractionContext(String message, Boolean success, Class<?> c, Exception e) {
		classContext = c;
		exceptionContext = e;
		if(message != null) {
			this.message = new StringBuilder();
			this.message.append(message);
		}
		this.success = success;
	}
	
	/**
	 * @return Class relevant to this context, or null
	 */
	public Class<?> getClassContext() {
		return classContext;
	}
	
	/**
	 * @return Exception relevant to this context, or null
	 */
	public Exception getExceptionContext() {
		return exceptionContext;
	}
	
	@Override
	public String getMessage() {
		return message.toString();
	}
	
	@Override
	public String getLocalizedMessage() {
		if(hasExceptionContext()) {
			return getExceptionContext().getMessage();
		}
		return null;
	}
	
	@Override
	public Exception getCause() {
		return getExceptionContext();
	}

	/**
	 * @return Target Object referenced by this context, or null
	 */
	public Object getTarget() {
		return target;
	}
	
	/**
	 * @return Previous Object referenced by this context, or null
	 */
	public Object getPrevious() {
		return previous;
	}

	/**
	 * @return The boolean representing the success state of this
	 * context, or null
	 */
	public Boolean isSuccess() {
		return success;
	}
	
	/**
	 * @return True if this context references a Class, else false
	 */
	public Boolean hasClassContext() {
		return classContext != null;
	}
	
	/**
	 * @return True if this context references an Exception, else false
	 */
	public Boolean hasExceptionContext() {
		return exceptionContext != null;
	}
	
	/**
	 * @return True if this context references a success state, else false
	 */
	public Boolean hasSuccess() {
		return success != null;
	}
	
	/**
	 * @return True if this context contains a detail message, else false
	 */
	public Boolean hasMessage() {
		return message != null;
	}
	
	/**
	 * @return True if this context references a target Object, else false
	 */
	public Boolean hasTarget() {
		return target != null;
	}
	
	/**
	 * @return True if this context references a previous Object, else false
	 */
	public Boolean hasPrevious() {
		return previous != null;
	}
	
	/**
	 * Adds message context to this instance.
	 * 
	 * @param message The message context to add
	 * @return The instance referenced
	 */
	public InteractionContext context(String message) {
		if(this.message == null) {
			this.message = new StringBuilder();
		} else {
			this.message.append('\n');
		}
		this.message.append(message);
		
		return this;
	}
	
	/**
	 * Adds formatted message context to this instance.
	 * 
	 * <p> The provided message string and arguments are processed with {@link String#format}
	 * before being added to this instance.</p>
	 * 
	 * @param message The format string content 
	 * @param args The arguments for the format string
	 * @return The instance referenced
	 * @see String#format(String, Object...)
	 */
	public InteractionContext context(String message, Object... args) {
		return context(String.format(message, args));
	}
	
	/**
	 * Adds success context to this instance.
	 * 
	 * @param success The success context to add
	 * @return The instance referenced
	 */
	public InteractionContext context(Boolean success) {
		this.success = success;
		
		return this;
	}
	
	/**
	 * Adds Class context to this instance.
	 * 
	 * @param classContext The Class context to add
	 * @return The instance referenced
	 */
	public InteractionContext context(Class<?> classContext) {
		this.classContext = classContext;
		
		return this;
	}
	
	/**
	 * Adds Exception context to this instance.
	 * 
	 * @param exceptionContext The Exception context to add
	 * @return The instance referenced
	 */
	public InteractionContext context(Exception exceptionContext) {
		this.exceptionContext = exceptionContext;
		
		return this;
	}
	
	public InteractionContext target(Object o) {
		target = o;
		
		return this;
	}
	
	public InteractionContext previous(Object o) {
		previous = o;
		
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
		case PREVIOUS:
			return hasPrevious();
		default:
			return false;
		}
	}
	
	/**
	 * This function accepts a consumer which takes an InteractionContext,
	 * followed by a variadic array of Context elements that determines
	 * under which contexts the consumer will be executed.
	 * 
	 * <p> The InteractionContext passed to the consumer is the same as
	 * the one upon which the function call was made. The consumer will
	 * be invoked at most once, if and only if at least one context in
	 * the provided array exists within this InteractionContext. </p>
	 * 
	 * <p> If no Context is provided, the consumer will never be invoked.</p>
	 * 
	 * @param consumer The lambda with which to consume the context
	 * @param context Zero or more contexts upon which to invoke the consumer
	 * @return The context within which this invocation takes place
	 * @see InteractionContext
	 * @see Context
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
	
	/**
	 * This function accepts a consumer which takes an InteractionContext,
	 * followed by a variadic array of Context elements that determines
	 * under which contexts the consumer will be executed.
	 * 
	 * <p> The InteractionContext passed to the consumer is the same as
	 * the one upon which the function call was made. The consumer will
	 * be invoked at most once, if and only if every context in the provided
	 * array exists within this InteractionContext. </p>
	 * 
	 * <p> If no Context is provided, the consumer will never be invoked.</p>
	 * 
	 * @param consumer The lambda with which to consume the context
	 * @param context Zero or more contexts upon which to invoke the consumer
	 * @return The context within which this invocation takes place
	 * @see InteractionContext
	 * @see Context
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
	
	public InteractionContext onException(BiConsumer<InteractionContext, Exception> consumer) throws InteractionContext {
		return onAnyContext(consume -> {
			consumer.accept(this, getExceptionContext());
		}, Context.EXCEPTION);
	}
	
	public InteractionContext onException(Consumer<Exception> consumer) throws InteractionContext {
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
	
	public InteractionContext onPrevious(BiConsumer<InteractionContext, Object> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(this, getPrevious());
		}, Context.PREVIOUS);
	}
	
	public InteractionContext onPrevious(Consumer<Object> consumer) {
		return onAnyContext(consume -> {
			consumer.accept(getPrevious());
		}, Context.PREVIOUS);
	}
	
	public <T> T andFinally(Function<InteractionContext, T> function) {
		return function.apply(this);
	}
}






































