package fr.landel.calc.processor;

import org.junit.jupiter.api.Test;

/**
 * test {@link MainProcessor}
 *
 * @since Jan 27, 2019
 * @author Gilles
 *
 */
public class LengthProcessorTest extends AbstractProcessorTest {

    /**
     * Test method for {@link MainProcessor#process(java.lang.String)}.
     * 
     * @throws ProcessorException
     *             on processing error
     */
    @Test
    void testProcess() throws ProcessorException {

        MainProcessor.setPrecision(3);
        MainProcessor.setUnityAbbrev(true);
        MainProcessor.setUnitiesSpace(false);
        MainProcessor.setValuesSpace(false);

        check("5m + 2ft - 2in", "5.559m");
        check("(5m + 2ft - 2in) >> ftin", "18.237ft");
    }
}
