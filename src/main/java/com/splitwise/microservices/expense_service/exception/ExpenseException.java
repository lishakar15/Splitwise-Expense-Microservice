package com.splitwise.microservices.expense_service.exception;

public class ExpenseException extends Exception{

    public ExpenseException(String message)
    {
        super(message);
    }
    public ExpenseException(String message,Throwable throwable)
    {
        super(message,throwable);
    }
}
