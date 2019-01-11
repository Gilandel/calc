package fr.landel.calc.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import fr.landel.calc.config.Formula;
import fr.landel.calc.config.Result;

/**
 * test {@link MainProcessor}
 *
 * @since Jan 1, 2019
 * @author Gilles
 *
 */
public class ProcessorTest {

    /**
     * Test method for {@link MainProcessor#process(java.lang.String)}.
     * 
     * @throws ProcessorException
     *             on processing error
     */
    // @Test
    void testProcess() throws ProcessorException {
        MainProcessor processor = new MainProcessor();

        assertThrows(ProcessorException.class, () -> processor.process("test(12)"), "Function not found: test");

        processor.setPrecision(0);

        check(processor.process("abs(12)"), "12");
        check(processor.process("abs(-12)"), "12");
        check(processor.process("abs(-12.645555)"), "13");

        processor.setPrecision(3);

        check(processor.process("abs(12)"), "12.000");
        check(processor.process("abs(-12)"), "12.000");
        check(processor.process("abs(-12.645555)"), "12.646");

        processor.setPrecision(4);

        check(processor.process("abs(12)"), "12.0000");
        check(processor.process("abs(-12)"), "12.0000");
        check(processor.process("abs(-12.645555)"), "12.6456");

        processor.setPrecision(15);

        check(processor.process("abs(12)"), "12.000000000000000");
        check(processor.process("abs(-12)"), "12.000000000000000");
        check(processor.process("abs(-12.645555)"), "12.645555000000000");
    }

    private void check(final Formula formula, final String expected) {
        final Optional<Result> result = formula.getResult();
        assertTrue(result.isPresent());
        assertTrue(result.get().isSuccess());
        assertEquals(expected, result.get().getResult());
    }
}
