package org.example.Commands;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandName {

    String value();

    int argsCount();

    int minStackSize() default 0;
}
