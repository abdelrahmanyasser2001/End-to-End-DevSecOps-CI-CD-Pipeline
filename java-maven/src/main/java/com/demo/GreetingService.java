package com.demo;

public class GreetingService {

    public String createGreeting(String name) {
        if (name == null || name.isBlank()) {
            return "Hello, Guest!";
        }

        return "Hello, " + name.trim() + "!";
    }
}