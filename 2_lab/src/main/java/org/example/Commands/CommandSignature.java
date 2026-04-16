package org.example.Commands;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandSignature {
    Class<?>[] value(); // Типы аргументов execute
}
