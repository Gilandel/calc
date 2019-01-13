package fr.landel.calc.utils;

import java.time.Duration;
import java.time.Period;

public class Interval {

    private final Duration duration;
    private final Period period;

    public Interval(final int years, final int months, final int days, final long hours, final long minutes, final long seconds, final long nanoseconds) {
        this.period = Period.of(years, months, days);
        this.duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusNanos(nanoseconds);
    }

    public double getValue() {
        return this.period.toTotalMonths() * DateUtils.NANO_PER_MONTH_AVG + this.period.getDays() * DateUtils.NANO_PER_DAY + this.duration.toNanos();
    }

    public int getYears() {
        return this.period.getYears();
    }

    public int getMonths() {
        return this.period.getMonths();
    }

    public int getDays() {
        return this.period.getDays();
    }

    public long getHours() {
        return this.duration.toHoursPart();
    }

    public long getMinutes() {
        return this.duration.toMinutesPart();
    }

    public long getSeconds() {
        return this.duration.toSecondsPart();
    }

    public long getNanoseconds() {
        return this.duration.toNanosPart();
    }

    @Override
    public String toString() {
        return this.period.toString() + "T" + this.duration.toString();
    }
}
