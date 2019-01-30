package fr.landel.calc.processor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import fr.landel.calc.utils.DateUtils;

/**
 * test {@link MainProcessor}
 *
 * @since Jan 27, 2019
 * @author Gilles
 *
 */
public class DateProcessorTest extends AbstractProcessorTest {

    /**
     * Test method for {@link MainProcessor#process(java.lang.String)}.
     * 
     * @throws ProcessorException
     *             on processing error
     */
    @Test
    void testProcess() throws ProcessorException {

        MainProcessor.setPrecision(0);

        MainProcessor.setUnityAbbrev(true);
        MainProcessor.setUnitiesSpace(false);
        MainProcessor.setValuesSpace(true);

        check("($m-($m=19h30-12h35+11h56-8h12-10h))>>hi", "18h 51i");
        check("(10h+19h30-(19h30-12h35+11h56-8h12))>>hi", "18h 51i");
        check("$m>>hi", "19h 30i");

        MainProcessor.setValuesSpace(false);

        check("$m", "19h30i");

        MainProcessor.setUnitiesSpace(true);
        MainProcessor.setUnityAbbrev(false);
        MainProcessor.setValuesSpace(true);

        check("(8h12+10h-11h56+12h35)>>hi", "18 hour 51 minute");

        // LocalDateTime.parse("2007-12-03T10:15:30");
        Entity entity1 = new Entity(0, "2007y1M");
        Entity entity2 = new Entity(0, "2007y4M");

        System.out.println((entity2.getValue() - entity1.getValue()) + " : " + DateUtils.NANO_PER_MONTHS_SUM.get(2));

        double annee = DateUtils.toZeroNanosecond(2007) + DateUtils.NANO_PER_MONTHS_SUM.get(11);
        double annee1 = LocalDateTime.of(2017, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC) * 1_000_0000_000d + DateUtils.NANO_EPOCH;
        double annee2 = LocalDateTime.of(2000, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC) * 1_000_0000_000d + DateUtils.NANO_EPOCH;

        Double y = (annee1 - DateUtils.NANO_EPOCH) / 1_000_0000_000d;
        Double y1 = (annee1 - annee2) / 1_000_0000_000d;
        // long s = y ;
        LocalDateTime date = LocalDateTime.ofEpochSecond(y.longValue(), 0, ZoneOffset.UTC);

        System.out.printf("%,.6f%n%,.6f%n%,.6f%n%,.6f%n%s", entity1.getValue(), annee, annee1, annee2, date);
    }
}
