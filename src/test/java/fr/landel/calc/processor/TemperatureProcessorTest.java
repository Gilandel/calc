package fr.landel.calc.processor;

import org.junit.jupiter.api.Test;

import fr.landel.calc.config.I18n;

/**
 * test {@link MainProcessor}
 *
 * @since Jan 27, 2019
 * @author Gilles
 *
 */
public class TemperatureProcessorTest extends AbstractProcessorTest {

    /**
     * Test method for {@link MainProcessor#process(java.lang.String)}.
     * 
     * @throws ProcessorException
     *             on processing error
     */
    @Test
    void testProcess() throws ProcessorException {

        checkException("(5C + 2K - 2Re) >> M", I18n.ERROR_OPERATOR.getI18n("4.500 K", "M", ">>"));
        checkException("(5C + 2K * 2Re)", I18n.ERROR_OPERATOR.getI18n("2.000 K", "2.000 Re", "*"));
        checkException("(2 / 4K) >> K", I18n.ERROR_OPERATOR.getI18n("2.000", "4.000 K", "/"));
        checkException("(3 % 4K) >> K", I18n.ERROR_OPERATOR.getI18n("3.000", "4.000 K", "%"));
        checkException("(4 ^ 2K) >> K", I18n.ERROR_OPERATOR.getI18n("4.000", "2.000 K", "^"));
        checkException("(2K / 4K) >> K", I18n.ERROR_OPERATOR.getI18n("2.000 K", "4.000 K", "/"));
        checkException("(3K % 4K) >> K", I18n.ERROR_OPERATOR.getI18n("3.000 K", "4.000 K", "%"));
        checkException("(4K ^ 2K) >> K", I18n.ERROR_OPERATOR.getI18n("4.000 K", "2.000 K", "^"));

        MainProcessor.setPrecision(3);
        MainProcessor.setUnityAbbrev(true);
        MainProcessor.setUnitiesSpace(false);
        MainProcessor.setValuesSpace(false);

        check("(5C + 2K - 2Re) >> K", "4.500K");
        check("(5C + 2K - 2Re * 2) >> K", "2.000K");
        check("(4K / 2) >> K", "2.000K");
        check("(4K % 3) >> K", "1.000K");
        check("(4K ^ 2) >> K", "16.000K");

        MainProcessor.setPrecision(0);
        MainProcessor.setUnityAbbrev(false);
        MainProcessor.setUnitiesSpace(true);
        MainProcessor.setValuesSpace(false);

        check("(5C + 2K - 2Re) >> C", "-269 celsius");
    }
}
