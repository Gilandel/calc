package fr.landel.calc.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import fr.landel.calc.config.I18n;
import fr.landel.calc.utils.StringUtils;

/**
 * test {@link Unity}
 *
 * @since Jan 27, 2019
 * @author Gilles
 *
 */
public class UnityTest {

    /**
     * Test method for {@link MainProcessor#process(java.lang.String)}.
     * 
     * @throws ProcessorException
     *             on processing error
     */
    @Test
    void testProcess() throws ProcessorException {

        assertEquals(Unity.DATE_MICROSECONDS, Unity.REDUCER.apply(Unity.DATE_HOURS, Unity.DATE_MICROSECONDS));
        assertEquals(Unity.DATE_MICROSECONDS, Unity.REDUCER.apply(Unity.DATE_MICROSECONDS, Unity.DATE_HOURS));

        String[] tests = {"minutes", "minute", "mile", "mi", "m", "i", "his", "mi"};
        String[] expected = {"minute; second", "minute", "mile", "mile", "meter", "minute", "hour; minute; second", "mile"};

        for (int i = 0; i < tests.length; ++i) {
            assertEquals(expected[i], Unity.getUnities(tests[i]).stream().map(Unity::longestSymbol).collect(StringUtils.SEMICOLON_JOINING_COLLECTOR));
        }

        try {
            Unity.getUnities("mi", UnityType.DATE);
            fail("Unities method has to throw an exception");

        } catch (ProcessorException e) {
            assertEquals(I18n.ERROR_UNITY_PARSE_TYPE.getI18n("mi", UnityType.DATE), e.getMessage());
        }

        assertEquals("mile", Unity.getUnities("mi").stream().map(Unity::longestSymbol).collect(StringUtils.SEMICOLON_JOINING_COLLECTOR));
    }
}
