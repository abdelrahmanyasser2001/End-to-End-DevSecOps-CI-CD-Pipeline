package com.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingServiceTest {

    private final GreetingService greetingService =
            new GreetingService();

    @Test
    void shouldCreateGreetingForProvidedName() {
        String result =
                greetingService.createGreeting("Abdelrahman");

        assertEquals("Hello, Abdelrahman!", result);
    }

    @Test
    void shouldRemoveSpacesAroundName() {
        String result =
                greetingService.createGreeting("  Abdelrahman  ");

        assertEquals("Hello, Abdelrahman!", result);
    }

    @Test
    void shouldUseGuestWhenNameIsEmpty() {
        String result = greetingService.createGreeting("");

        assertEquals("Hello, Guest!", result);
    }

    @Test
    void shouldUseGuestWhenNameContainsOnlySpaces() {
        String result = greetingService.createGreeting("   ");

        assertEquals("Hello, Guest!", result);
    }

    @Test
    void shouldUseGuestWhenNameIsNull() {
        String result = greetingService.createGreeting(null);

        assertEquals("Hello, Guest!", result);
    }
}