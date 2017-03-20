package net.kuronicle.etl.test.exception;

public class EtlTesterException extends RuntimeException {

    public EtlTesterException(String message) {
        super(message);
    }

    public EtlTesterException(String message, Throwable cause) {
        super(message, cause);
    }
}
