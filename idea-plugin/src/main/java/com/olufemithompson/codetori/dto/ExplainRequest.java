package com.olufemithompson.codetori.dto;

public class ExplainRequest {

    private String function;

    public ExplainRequest(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
