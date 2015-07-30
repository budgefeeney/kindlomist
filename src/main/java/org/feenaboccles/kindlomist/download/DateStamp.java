package org.feenaboccles.kindlomist.download;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.Economist;

import javax.validation.ValidationException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

/**
 * A validated date-stamp value. Valid datestamps are strings in the format yyyy-mm-dd that
 * denote a valid date between 1900-01-01 and today's current date.
 */
public final class DateStamp {

    public static final String VALIDATION_ERRMSG = "The date-stamp must be in the format yyyy-mm-dd";
    public static final int MIN_YEAR = 1900;
    private final String value;
    private final LocalDate localDate;

    private DateStamp(@NonNull String value) {
        value = value.trim();
        LocalDateTime now = LocalDateTime.now();

        if (value.length() != 10)
            throw new ValidationException (VALIDATION_ERRMSG);
        String[] parts = StringUtils.split(value, '-');
        if (parts.length != 3)
            throw new ValidationException (VALIDATION_ERRMSG);

        try {
            int year = Integer.parseInt(parts[0]);
            if (year < MIN_YEAR || year > now.getYear())
                throw new ValidationException("The year must be between " + MIN_YEAR + " and " + now.getYear());

            int month = Integer.parseInt(parts[1]);
            if (month <= 0 || month > 12)
                throw new ValidationException(VALIDATION_ERRMSG + ", where the month is in the range 1 to 12");

            int dayOfMonth = Integer.parseInt(parts[2]);
            if (dayOfMonth <= 0 || dayOfMonth > 31)
                throw new ValidationException(VALIDATION_ERRMSG + ", where the day-of-month is in the range 1 to 12");

            localDate = LocalDate.of(year, month, dayOfMonth);

            // figure out the maximum allowable date. The Economist releases issues on Thursdays
            // with datestamps referring to the following saturdays
            LocalDate maxDate = maxDateTime(now);

            if (localDate.isAfter(maxDate))
                throw new ValidationException(VALIDATION_ERRMSG + ", and must not be after " + now);

            this.value = value;
        } catch (NumberFormatException e) {
            throw new ValidationException (VALIDATION_ERRMSG);
        } catch (DateTimeException e) {
            throw new ValidationException (VALIDATION_ERRMSG + ", but the date-stamp provided is not a valid date - check your month and day-of-month");
        }
    }

    /**
     * Determine the maximum permissable date allowed. This is complicated by the fact
     * that the Economist is released on one day (a Thursday) with a time-stamp set in
     * the <em>future</em> (a Saturday).
     */
    public static LocalDate maxDateTime (LocalDateTime time) {
        int releaseDay  = Economist.PUBLICATION_DAY.getValue();
        int declaredDay = Economist.DECLARED_PUBLICATION_DAY.getValue();

        int declaredJump = declaredDay < releaseDay
            ? (declaredDay + 7) - releaseDay
            : declaredDay - releaseDay;

        int today = time.getDayOfWeek().getValue();
        if (time.getHour() < Economist.PUBLICATION_HOUR) { // arithmetic assumes this all happens on the stroke of midnight
            today = today > 0 ? today - 1 : 7;             // so go a few hours into the "past" to adjust
        }

        int adjust = ((today + declaredJump) % 7 <= declaredDay) ? +1 : -1;
        while (time.getDayOfWeek() != Economist.DECLARED_PUBLICATION_DAY) {
            time = time.plus(adjust, ChronoUnit.DAYS);
        }

        return time.toLocalDate();
    }

    public static DateStamp of (String value) throws ValidationException {
        return new DateStamp(value);
    }

    public static DateStamp of (LocalDate value) throws ValidationException {
        return new DateStamp(value.toString());
    }

    /**
     * The text value of a date-stamp in the format "yyyy-mm-dd"
     */
    public String value() {
        return value;
    }

    /**
     * The text value of a date-stamp in the format "yyyymmdd"
     */
    public String valueAsNumbersOnly() {
        return value.replaceAll("\\D", "");
    }

    /**
     * The date encoded by the date-stamp as a LocalDate object
     */
    public LocalDate asLocalDate() {
        return localDate;
    }

    @Override public String toString() {
        return value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateStamp dateStamp = (DateStamp) o;

        return !(localDate != null ? !localDate.equals(dateStamp.localDate) : dateStamp.localDate != null);

    }

    @Override
    public int hashCode() {
        return localDate != null ? localDate.hashCode() : 0;
    }
}
