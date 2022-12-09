package com.testrigor.selfhealingselenium.infrastructure.exceptions;

public class SelfHealingException extends RuntimeException {

	public SelfHealingException() {
		super();
	}

	public SelfHealingException(String message) {
		super(message);
	}

	public SelfHealingException(String message, Throwable cause) {
		super(message, cause);
	}

	public SelfHealingException(Throwable cause) {
		super(cause);
	}

	protected SelfHealingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
