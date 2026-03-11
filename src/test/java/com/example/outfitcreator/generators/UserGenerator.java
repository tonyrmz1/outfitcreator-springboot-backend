package com.example.outfitcreator.generators;

import com.example.outfitcreator.entity.User;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.time.LocalDateTime;

/**
 * jqwik generator for User entities with valid email and password formats.
 */
public class UserGenerator {
    
    /**
     * Generate valid User entities with realistic data.
     */
    public static Arbitrary<User> users() {
        return Combinators.combine(
            validEmails(),
            validPasswords(),
            optionalFirstNames(),
            optionalLastNames()
        ).as((email, password, firstName, lastName) -> 
            User.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
    }
    
    /**
     * Generate valid email addresses.
     * Format: username@domain.tld
     */
    public static Arbitrary<String> validEmails() {
        Arbitrary<String> username = Arbitraries.strings()
            .alpha()
            .numeric()
            .withChars("._-")
            .ofMinLength(3)
            .ofMaxLength(20);
        
        Arbitrary<String> domain = Arbitraries.of(
            "gmail.com", "yahoo.com", "outlook.com", "example.com", 
            "test.com", "mail.com", "email.com"
        );
        
        return Combinators.combine(username, domain)
            .as((user, dom) -> user + "@" + dom);
    }
    
    /**
     * Generate valid passwords (8-20 characters, alphanumeric).
     */
    public static Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .withChars("!@#$%^&*")
            .ofMinLength(8)
            .ofMaxLength(20);
    }
    
    /**
     * Generate optional first names (can be null).
     */
    public static Arbitrary<String> optionalFirstNames() {
        return Arbitraries.of(
            "John", "Jane", "Michael", "Sarah", "David", "Emily", 
            "James", "Emma", "Robert", "Olivia", "William", "Sophia"
        ).injectNull(0.2);
    }
    
    /**
     * Generate optional last names (can be null).
     */
    public static Arbitrary<String> optionalLastNames() {
        return Arbitraries.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez"
        ).injectNull(0.2);
    }
    
    /**
     * Generate users with specific email patterns for testing.
     */
    public static Arbitrary<User> usersWithEmail(String email) {
        return Combinators.combine(
            Arbitraries.just(email),
            validPasswords(),
            optionalFirstNames(),
            optionalLastNames()
        ).as((e, password, firstName, lastName) -> 
            User.builder()
                .email(e)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
    }
}
