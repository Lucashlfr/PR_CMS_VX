package com.messdiener.cms.v3.utils.time;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

@AllArgsConstructor
@Data
public class CMSDate {


    private long date;

    public CMSDate(String date, DateUtils.DateType dateType){
        this.date = DateUtils.convertDateToLong(date, dateType);
    }

    public static CMSDate of(long date) {
        return new CMSDate(date);
    }

    public static CMSDate current(){
        return new CMSDate(System.currentTimeMillis());
    }

    public static CMSDate convert(String date, DateUtils.DateType dateType) {
        return new CMSDate(date, dateType);
    }

    public String getGermanDate(){
        return DateUtils.convertLongToDate(date, DateUtils.DateType.GERMAN);
    }

    public long toLong(){
        return date;
    }

    public String getEnglishDate(){
        return DateUtils.convertLongToDate(date, DateUtils.DateType.ENGLISH);
    }

    public String getGermanLongDate(){
        return DateUtils.convertLongToDate(date, DateUtils.DateType.GERMAN_WITH_DAY_NAME);
    }
    public String getGermanShortDate(){
        return DateUtils.convertLongToDate(date, DateUtils.DateType.GERMAN_WITH_DAY_NAME_S);
    }


    public String getDayDate() {
        return DateUtils.convertLongToDate(date, DateUtils.DateType.GERMAN_WITH_DAY_TIME);
    }

    public String getEnglishDayDate() {
        return DateUtils.convertLongToDate(date, DateUtils.DateType.ENGLISH_DATETIME);
    }

    public String getTime() {
        return DateUtils.convertLongToDate(date, DateUtils.DateType.TIME);
    }

    public String convertTo(DateUtils.DateType dateType){
        return DateUtils.convertLongToDate(date, dateType);
    }

    public int getAge(){
        return DateUtils.getAge(date);
    }

    public DateUtils.MonthNumberName getMonthName(){
        return DateUtils.getMonthNumberName(date);
    }

    public String monthName(){
        return convertTo(DateUtils.DateType.MONTH_NAMES);
    }

    public int getDayOfWeek() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
    }

    public static Optional<CMSDate> generateOptional(long date){
        CMSDate cmsDate = of(date);
        if(cmsDate.getGermanDate().equals("01.01.1970"))
            return Optional.empty();
        return Optional.of(cmsDate);
    }

    public static Optional<CMSDate> generateOptionalString(Optional<String> date, DateUtils.DateType dateType){
        if(date.isEmpty())return Optional.empty();
        String dateString = date.get();
        CMSDate cmsDate = convert(dateString, dateType);
        if(cmsDate.getGermanDate().equals("01.01.1970"))
            return Optional.empty();
        return Optional.of(cmsDate);
    }

    public String getGermanTime(){
        return convertTo(DateUtils.DateType.GERMAN_WITH_TIME);
    }

    public String getDay(){
        return convertTo(DateUtils.DateType.DAY);
    }

    public String getGt(){
        return convertTo(DateUtils.DateType.GT);
    }

    public String getGermanWithSeconds(){
        return convertTo(DateUtils.DateType.GERMAN_WITH_SECONDS);
    }

}
