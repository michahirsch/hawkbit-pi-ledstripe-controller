package org.michahirsch.hawkbit.ledstripe.ddi.client;

public class ScriptExecutionException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private final int retCode;

    public ScriptExecutionException(final int retCode, final String message) {
        super(message);
        this.retCode = retCode;
    }

    public int getRetCode() {
        return retCode;
    }
}
