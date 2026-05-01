package com.ai.template.agent.context;

import java.util.function.Consumer;


public class StreamHandlerContext {

    private static final ThreadLocal<Consumer<String>> STREAM_HANDLER = new ThreadLocal<>();

    
    public static void set(Consumer<String> handler) {
        STREAM_HANDLER.set(handler);
    }

    
    public static Consumer<String> get() {
        return STREAM_HANDLER.get();
    }

    
    public static void clear() {
        STREAM_HANDLER.remove();
    }

    
    public static void send(String message) {
        Consumer<String> handler = STREAM_HANDLER.get();
        if (handler != null && message != null) {
            handler.accept(message);
        }
    }
}
