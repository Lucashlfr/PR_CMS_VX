package com.messdiener.cms.v3.utils.time;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class DateUtils {



    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    @Getter
    public enum MonthNumberName {
        JANUAR(1, "Januar", "01.01", "31.01"),
        FEBRUARY(2, "Februar", "01.02", "28.02"),
        MARCH(3, "März", "01.03", "31.03"),
        APRIL(4, "April", "01.04", "30.04"),
        MAY(5, "Mai", "01.05", "31.05"),
        JUNE(6, "Juni", "01.06", "30.06"),
        JULY(7, "Juli", "01.07", "31.07"),
        AUGUST(8, "August", "01.08", "31.08"),
        SEPTEMBER(9, "September", "01.09", "30.09"),
        OCTOBER(10, "Oktober", "01.10", "31.10"),
        NOVEMBER(11, "November", "01.11", "30.11"),
        DECEMBER(12, "Dezember", "01.12", "31.12");

        private final int number;
        private final String name;
        private final long firstDay;
        private final long lastDay;

        MonthNumberName(int number, String name, String firstDay, String lastDay) {
            this.number = number;
            this.name = name;
            this.firstDay = DateUtils.convertDateToLong(firstDay+"."+DateUtils.getCurrentYear()+" 00:00", DateType.GERMAN_WITH_TIME);
            this.lastDay = DateUtils.convertDateToLong(lastDay+"."+DateUtils.getCurrentYear() + " 23:59", DateType.GERMAN_WITH_TIME);
        }

        public static MonthNumberName getByMonth(int month) {
            return MonthNumberName.values()[month];
        }

        public CMSDate firstDayCMS(){
            return CMSDate.of(firstDay);
        }

    }

    @Getter
    public enum DateType{
        ENGLISH("yyyy-MM-dd"),
        GERMAN("dd.MM.yyyy"),
        GERMAN_WITH_TIME("dd.MM.yyyy HH:mm"),
        GERMAN_WITH_DAY_NAME("EEEE, dd.MM.yyyy"),
        ENGLISH_DATETIME("yyyy-MM-dd'T'HH:mm"),
        TIME("HH:mm"),
        GERMAN_WITH_DAY_TIME("EEEE, dd.MM.yyyy HH:mm"),
        MONTH_NAMES("MMMM yyyy"),
        SIMPLE_GERMAN("E, dd.MM.yy"),
        DAY("EEEE"),
        GT("dd MMMM, yyyy HH:mm 'Uhr'");

        private final String pattern;

        DateType(String pattern){
            this.pattern = pattern;
        }

    }

    public static String convertLongToDate(long millis, DateType dateType){
        Date date = new Date(millis);
        DateFormat dateFormat = new SimpleDateFormat(dateType.getPattern(), Locale.GERMAN);
        return dateFormat.format(date);
    }

    public static long convertDateToLong(String date, DateType dateType) {
        DateFormat dateFormat = new SimpleDateFormat(dateType.getPattern());

        try {
            return dateFormat.parse(date).getTime();
        } catch (ParseException e) {
            logger.error(e.getMessage());
            return -1;
        }
    }

    public static MonthNumberName getMonthNumberName(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int month = cal.get(Calendar.MONTH)+1;

        for (MonthNumberName v: MonthNumberName.values()) {
            if(v.getNumber() == month)
                return v;
        }
        return null;
    }

    public static MonthNumberName getMonthNumberName(long date){
        return getMonthNumberName(new Date(date));
    }

    public static int getCurrentMonthNumber(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));

        return cal.get(Calendar.MONTH)+1;
    }

    public static int getAge(long birthday) {
        LocalDate birthdayDate = createLocalDate(birthday);
        LocalDate current = createLocalDate(System.currentTimeMillis());

        return Period.between(birthdayDate, current).getYears();

    }

    private static LocalDate createLocalDate(long millis){
        String[] bArray = convertLongToDate(millis, DateType.ENGLISH).split("-");
        int[] iArray = new int[bArray.length];
        for (int i = 0; i < bArray.length; i++) {
            iArray[i] = Integer.parseInt(bArray[i]);
        }
        return LocalDate.of(iArray[0], iArray[1], iArray[2]);
    }

    public static int getCurrentYear(){
        return Integer.parseInt(DateUtils.convertLongToDate(System.currentTimeMillis(), DateType.ENGLISH).split("-")[0])+1;
    }

    public static int nextYear(){
        return getCurrentYear()+1;
    }

    public static MonthNumberName getMonthByNumber(int monthNumber) {
        return MonthNumberName.values()[monthNumber-1];
    }

    public static CMSDate getFirstDateByNumber(int monthNumber) {
        return MonthNumberName.values()[monthNumber-1].firstDayCMS();
    }

    public static Map<String, Long> generateMonthRange(Long startDate, Long endDate) {
        // Konvertiere die Long-Werte (Millisekunden) in LocalDate-Objekte
        LocalDate start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();

        // Überprüfe, ob das Enddatum vor dem Startdatum liegt
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must not be before start date");
        }

        Map<String, Long> months = new LinkedHashMap<>();

        // Iteriere durch die Monate im Bereich
        LocalDate current = start; // Beginne direkt mit dem Startdatum
        while (!current.isAfter(end)) {
            String monthYear = current.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + current.getYear();
            long dateValue = current.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            months.put(monthYear, dateValue);
            current = current.plusMonths(1); // Gehe einen Monat weiter
        }

        // Sortiere die Map nach den Werten (Long) und gebe eine neue Map zurück
        return months.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // Konfliktlösung: keine doppelten Einträge erwartet
                        LinkedHashMap::new // Erhalte die Sortierung
                ));
    }

    public static long[] getFirstAndLastDayOfMonth(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Set zum ersten Tag des Monats
        calendar.set(year, month, 1, 0, 0, 0);
        long firstDayMillis = calendar.getTimeInMillis();

        // Set zum letzten Tag des Monats
        calendar.set(year, month + 1, 1, 0, 0, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long lastDayMillis = calendar.getTimeInMillis();

        return new long[]{firstDayMillis, lastDayMillis};
    }

    public static List<String> generateMonths(List<CMSDate> dates) {
        Set<String> monthYearSet = new HashSet<>();
        for (CMSDate timestamp : dates) {
            monthYearSet.add(timestamp.convertTo(DateType.MONTH_NAMES));
        }
        return new ArrayList<>(monthYearSet);
    }




}
