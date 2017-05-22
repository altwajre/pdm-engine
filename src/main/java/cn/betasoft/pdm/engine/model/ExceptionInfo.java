package cn.betasoft.pdm.engine.model;

public class ExceptionInfo {

	private String methodName;

	private String arguments;

	private String message;

	private String stack;

	public ExceptionInfo(String methodName, String arguments, String message, String stack) {
		this.methodName = methodName;
		this.arguments = arguments;
		this.message = message;
		this.stack = stack;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStack() {
		return stack;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}
}
