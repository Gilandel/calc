package fr.landel.calc.processor;

import org.junit.jupiter.api.Test;

import fr.landel.calc.config.I18n;

/**
 * test {@link MainProcessor}
 *
 * @since Jan 1, 2019
 * @author Gilles
 *
 */
public class ProcessorTest extends AbstractProcessorTest {

    /**
     * Test method for {@link MainProcessor#process(java.lang.String)}.
     * 
     * @throws ProcessorException
     *             on processing error
     */
    @Test
    void testProcess() throws ProcessorException {

        checkException("test(12)", I18n.ERROR_FUNCTION_UNKNOWN.getI18n("test"));

        MainProcessor.setPrecision(0);
        check("12", "12");

        check("abs(12)", "12");
        check("abs(-12)", "12");
        check("abs(-12.645555)", "13");

        MainProcessor.setPrecision(3);
        check("abs(12)", "12.000");
        check("abs(-12)", "12.000");
        check("abs(-12.645555)", "12.646");

        MainProcessor.setPrecision(4);
        check("abs(12)", "12.0000");
        check("abs(-12)", "12.0000");
        check("abs(-12.645555)", "12.6456");

        MainProcessor.setPrecision(15);
        check("abs(12)", "12.000000000000000");
        check("abs(-12)", "12.000000000000000");
        check("abs(-12.645555)", "12.645555000000000");

        MainProcessor.setPrecision(3);
        check("2007y3M-2008y2M", "-11.079 M");
        check("100Y7M*2", "201 Y 2 M"); // XXX bug > 293 ans (long)
        check("3*(3+2)", "15.000");
        check("((3+2)*pow(9/abs(3);1-5))-2", "-1.938");
        check("((3)*(3-2))", "3.000");
        check("(3h/2)>>i", "90 i");
        check("15in>>m", "0.381 m");
        check("(15h+12s)>>his", "15 h 12 s");
        check("5K>>C", "-268.150 C");
        check("5C>>K", "278.150 K");
        check("15/(1200/3937/12)", "590.550");
        check("15m>>in", "590.550 in");
        check("2017y12M >>y", "2017 YA 11 MA 1 h 43 i 29 s 999 S 995 O 904 N"); // XXX bug
    }
}
