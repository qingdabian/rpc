package com.crabapple.core.client.circuitbreaker;

public enum CircuitBreakerState {
    OPEN("open"),
    CLOSE("close"),
    HALF_OPEN("halfOpen");
    private String state;
    private CircuitBreakerState(String state) {
        this.state = state;
    }
}
