package com.trees.core.validations;


public class TreeInvariantViolationException extends RuntimeException {

    public TreeInvariantViolationException(String message)
     {
        super(message);
    }

    public TreeInvariantViolationException(String message, Throwable cause)
    
    {
        super(message, cause);
    }
}