package org.example.client;


import java.io.IOException;


public class AuthException extends IOException {
    public AuthException(String reason) {
        super(reason);
    }
}