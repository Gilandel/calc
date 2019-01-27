package fr.landel.calc.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;

import fr.landel.calc.config.Formula;
import fr.landel.calc.config.Result;
import fr.landel.calc.utils.StringUtils;

/**
 * abstract class to test processor
 *
 * @since Jan 27, 2019
 * @author Gilles
 */
public abstract class AbstractProcessorTest {

    private MainProcessor processor;

    protected AbstractProcessorTest() {
        processor = new MainProcessor();
    }

    @BeforeEach
    public void init() {
        MainProcessor.setRadian(true);
        MainProcessor.setExact(true);
        MainProcessor.setScientific(true);
        MainProcessor.setPrecision(3);
        MainProcessor.setUnityAbbrev(true);
        MainProcessor.setUnitiesSpace(true);
        MainProcessor.setValuesSpace(true);
    }

    protected void checkException(final String expression, final String message) {
        try {
            processor.process(expression);
            fail(StringUtils.inject("Expression '{}' has to thrown error", expression));

        } catch (ProcessorException e) {
            assertEquals(message, e.getMessage());
        }
    }

    protected void check(final String expression, final String expected) {
        try {
            final Formula formula = processor.process(expression);
            final Optional<Result> result = formula.getResult();
            assertTrue(result.isPresent());
            assertTrue(result.get().isSuccess());
            assertEquals(expected, result.get().getResult());
        } catch (ProcessorException e) {
            fail(e);
        }
    }
}
