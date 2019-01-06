package fr.landel.calc.processor;

import fr.landel.calc.config.Formula;

public interface Processor {
    Formula process() throws ProcessorException;
}
