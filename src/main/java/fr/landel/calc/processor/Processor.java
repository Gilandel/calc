package fr.landel.calc.processor;

public interface Processor {
    Entity process() throws ProcessorException;
}
