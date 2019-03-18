package io.github.ksmail13.sse;

public enum EventType {
    EVENT, PING;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
