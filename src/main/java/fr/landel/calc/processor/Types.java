package fr.landel.calc.processor;

import java.util.regex.Pattern;

public enum Types {
    OPERATOR(null),
    NUMBER(Pattern.compile("((-?[0-9](:?\\.[0-9]+))(?:[Ee]([+-]?[0-9]+))?([a-zA-Z]+)?)")),
    DATE(null),;

    private final Pattern validator;

    private Types(final Pattern validator) {
        this.validator = validator;
    }
}
