package com.cetiti.utils;

import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss",
            YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss SSS",
            YYYY_MM_DD_HH_MM_SS_SSS_2 = "yyyy-MM-dd HH:mm:ss.SSS",
            YYYYMMDD = "yyyyMMdd",
            YYYYMMDD_HHMMSS = "yyyyMMdd_HHmmss",
            YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm",
            HH_MM_SS = "HH:mm:ss",
            MM_DD_HH_MM = "MM-dd HH:mm",
            YYYY_MM_DD = "yyyy-MM-dd",
            YYYY_MM_DD_00_00_00 = "yyyy-MM-dd 00:00:00",
            YYYY_MM_DD_23_59_59 = "yyyy-MM-dd 23:59:59",
            YYYY_MM = "yyyy-MM";

    public static final DateTimeFormatter defaultDtf = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    public static final DateTimeFormatter dtfYYYY_MM_DD = DateTimeFormatter.ofPattern(YYYY_MM_DD);
    public static final DateTimeFormatter dtfYYYY_MM_DD_00_00_00 = DateTimeFormatter.ofPattern(YYYY_MM_DD_00_00_00);
    public static final DateTimeFormatter dtfYYYY_MM_DD_23_59_59 = DateTimeFormatter.ofPattern(YYYY_MM_DD_23_59_59);

    public static final DateTimeFormatter defYYYY_MM_DD_HH_MM_SS_SSS_2 = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_SSS_2);

    public static SimpleDateFormat getSdf() {
        return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
    }

    public static final SimpleDateFormat getSdfShort() {
        return new SimpleDateFormat(YYYY_MM_DD);
    }

    public static final SimpleDateFormat getSdfShort2() {
        return new SimpleDateFormat(YYYYMMDD);
    }

    //public static final SimpleDateFormat sdfShort = new SimpleDateFormat(YYYY_MM_DD);

    /*Date相关 START****************************************************************************/

    /**
     * string转为date
     *
     * @param dateString
     * @return
     */
    public synchronized static Date string2Date(String dateString) {
        try {
            return getSdf().parse(dateString);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", e);
        }
        return null;
    }

    /**
     * 时间转字符串，格式自定义
     *
     * @param date
     * @param format
     * @return
     */
    public synchronized static String date2String(Date date, String format) {
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.getSdfShort2", e);
        }
        return null;
    }


    /**
     * 时间转字符串，返回格式：yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public synchronized static String date2ShortString(Date date) {
        try {
            return getSdfShort().format(date);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.date2ShortString", e);
        }
        return null;
    }

    /**
     * 时间转字符串，返回格式：yyyyMMdd
     *
     * @param date
     * @return
     */
    public synchronized static String date2ShortString2(Date date) {
        try {
            return getSdfShort2().format(date);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.getSdfShort2", e);
        }
        return null;
    }


    /**
     * 获取每天的开始时间 00:00:00:00
     *
     * @param date
     * @return
     */
    public static Date getStartTime(Date date) {
        Calendar dateStart = Calendar.getInstance();
        dateStart.setTime(date);
        dateStart.set(Calendar.HOUR_OF_DAY, 0);
        dateStart.set(Calendar.MINUTE, 0);
        dateStart.set(Calendar.SECOND, 0);
        return dateStart.getTime();
    }

    /**
     * 获取每天的结束时间 23:59:59:999
     *
     * @param date
     * @return
     */
    public static Date getEndTime(Date date) {
        Calendar dateEnd = Calendar.getInstance();
        dateEnd.setTime(date);
        dateEnd.set(Calendar.HOUR_OF_DAY, 23);
        dateEnd.set(Calendar.MINUTE, 59);
        dateEnd.set(Calendar.SECOND, 59);
        return dateEnd.getTime();
    }

    /**
     * 获取当前分钟开始时间
     *
     * @param date
     * @return
     */
    public static Date getMinuteStart(Date date) {
        Calendar dateStart = Calendar.getInstance();
        dateStart.setTime(date);
        dateStart.set(Calendar.SECOND, 0);
        return dateStart.getTime();
    }

    /**
     * 添加天数
     *
     * @param time
     * @param num
     * @return
     */
    public synchronized static Date addDate(Date time, int num) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(time);
        gc.add(5, num);
        return gc.getTime();
    }

    /**
     * @param time 时间参数, 格式yyyy-MM-dd
     * @param num  天数
     * @return 新的时间
     * @throws
     * @Title: addDate
     * @Description: TODO(时间加减天数)
     */
    public synchronized static String addDate(String time, int num) throws ParseException {
        try {
            Date date = StringToDate(time, YYYY_MM_DD);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(date);
            gc.add(5, num);
            return date2ShortString(gc.getTime());
        } catch (ParseException e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        System.out.println(date2LongString(getStartTime(addTime(new Date(), 99 * 12, "month"))));
        System.out.println(date2LongString(addTime(new Date(), 99, "year")));
    }

    /**
     * 时间加减
     *
     * @param time
     * @param num  正负都可
     * @param type hour：小时|second：秒|minute：分钟|day：天|week：周（七天）|month：月|year：年
     * @return
     */
    public synchronized static Date addTime(Date time, int num, String type) {
        int field;
        switch (type) {
            case "hour":
                field = Calendar.HOUR;
                break;
            case "second":
                field = Calendar.SECOND;
                break;
            case "minute":
                field = Calendar.MINUTE;
                break;
            case "day":
                field = Calendar.DATE;
                break;
            case "week":
                field = Calendar.DATE;
                num = num * 7;
                break;
            case "month":
                field = Calendar.MONTH;
                break;
            case "year":
                field = Calendar.YEAR;
                break;
            default:
                return time;
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(time);
        gc.add(field, num);
        return gc.getTime();
    }

    /**
     * 获取当前分钟结束时间
     *
     * @param date
     * @return
     */
    public static Date getMinuteEnd(Date date) {
        Calendar dateEnd = Calendar.getInstance();
        dateEnd.setTime(date);
        dateEnd.set(Calendar.SECOND, 59);
        return dateEnd.getTime();
    }

    /**
     * 格式转化
     *
     * @return String
     */
    public static String date2LongString(Date date) {
        return getSdf().format(date);
    }

    /**
     * @param time   时间字符串
     * @param format 对应时间字符串的时间格式,如"yyyy-MM-dd"
     * @return Date类型的时间
     * @throws ParseException
     * @throws ParseException 格式转换异常
     * @Title: StringToDate
     * @Description: TODO(将指定字符串格式转化为时间类型)
     */
    public synchronized static Date StringToDate(String time, String format) throws ParseException {
        SimpleDateFormat dataFormat = null;
        if (format == null || format.equals("")) {
            dataFormat = getSdf();
        } else {
            dataFormat = new SimpleDateFormat(format);
        }
        Date date = null;
        try {
            if (time != null && !time.equals("")) {
                date = dataFormat.parse(time);
            } else {
                return null;
            }
        } catch (ParseException e) {
            throw e;
        }
        return date;
    }


    /**
     * 计算时间差，转换成字符串，格式：XX天XX小时XX分XX秒
     *
     * @param dateBegin 开始时间
     * @param dateEnd   结束时间
     * @return
     */
    private static String getTimeBetweenStr(Date dateBegin, Date dateEnd) {
        long haveTime = dateBegin.getTime() - dateEnd.getTime();
        return getTimeBetweenStr(haveTime);
    }

    /**
     * JAVA计算两个日期相差多少天(by date)
     */
    public static int daysBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(Math.abs(between_days)));
    }

    /**
     * ================MONTH===================================================
     * /**
     *
     * @Title: toFirstDay @Description: TODO(将日期转化为指定月的第一天) @param date
     * 指定日期 @return Date 指定月的第一天 @throws
     */
    public synchronized static Date toFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar c = Calendar.getInstance();
        c.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1);
        return c.getTime();
    }

    /**
     * 获取一个月的最后一天
     *
     * @param date
     * @return
     */
    public synchronized static Date toLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);// 设置改约第一天
        cal.add(Calendar.MONTH, 1);// 在设置第一天后增加一个月
        cal.add(Calendar.DAY_OF_MONTH, -1);// 获取改约最后一天
        Date lastDayOfMonth = cal.getTime();
        return lastDayOfMonth;
    }

    /**
     * @Title: addMonth @Description: TODO(时间加减月数量) @param date 时间参数 @param n
     * 月数量 @return 返回新的时间 @throws
     */
    public synchronized static Date addMonth(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, n);
        return cal.getTime();
    }

    /**
     * 当前时间上个月
     *
     * @return
     */
    public static Date getLastMonth() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, -1);
        return c.getTime();
    }

    /*Date相关 END****************************************************************************/


    /*LocalDateTime相关 START****************************************************************************/

    /**
     * 时间转long
     *
     * @param value
     * @return
     */
    public static Long localDate2Long(LocalDateTime value) {
        try {
            return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.localDate2Long", e);
        }
        return null;
    }

    /**
     * long转时间
     *
     * @param timestamp
     * @return
     */
    public static LocalDateTime long2Localdate(long timestamp) {
        if (timestamp > 0) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        } else {
            return null;
        }
    }

    /**
     * 字符串转时间，字符串格式：yyyy-MM-dd HH:mm:ss
     *
     * @param dateString
     * @return
     */
    public synchronized static LocalDateTime string2LocalDate(String dateString) {
        try {
            return LocalDateTime.parse(dateString, defaultDtf);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", e);
        }
        return null;
    }

    /**
     * 字符串转时间，传入日期字符串（格式yyyyMM-dd），返回当天最早时刻
     *
     * @param dateString
     * @return
     */
    public synchronized static LocalDateTime string2LocalDateBegin(String dateString) {
        try {
            return LocalDateTime.parse(dateString + " 00:00:00", defaultDtf);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", e);
        }
        return null;
    }

    /**
     * 字符串转时间，传入日期字符串（格式yyyyMM-dd），返回当天最晚时刻
     *
     * @param dateString
     * @return
     */
    public synchronized static LocalDateTime string2LocalDateEnd(String dateString) {
        try {
            return LocalDateTime.parse(dateString + " 23:59:59", defaultDtf);
        } catch (Exception e) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", e);
        }
        return null;
    }

    /**
     * 时间转字符串，返回格式yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public synchronized static String localDate2string(LocalDateTime date) {
        try {
            return defaultDtf.format(date);
        } catch (Exception var2) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", var2);
            return null;
        }
    }

    /**
     * 格式化时间，返回年月日（yyyy-MM-dd）
     *
     * @param date
     * @return
     */
    public synchronized static String localDate2ShortString(LocalDateTime date) {
        try {
            return dtfYYYY_MM_DD.format(date);
        } catch (Exception var2) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", var2);
            return null;
        }
    }

    public synchronized static String localDate2LongString(LocalDateTime date) {
        try {
            return defYYYY_MM_DD_HH_MM_SS_SSS_2.format(date);
        } catch (Exception var2) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", var2);
            return null;
        }
    }

    /**
     * 获取当天最早时刻，并返回格式化后的字符串
     *
     * @param date
     * @return
     */
    public synchronized static String localDate2StringBegin(LocalDateTime date) {
        try {
            return dtfYYYY_MM_DD_00_00_00.format(date);
        } catch (Exception var2) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", var2);
            return null;
        }
    }

    /**
     * 获取当天最晚时刻，并返回格式化后的字符串
     *
     * @param date
     * @return
     */
    public synchronized static String localDate2StringEnd(LocalDateTime date) {
        try {
            return dtfYYYY_MM_DD_23_59_59.format(date);
        } catch (Exception var2) {
            LoggerFactory.getLogger(DateUtils.class).error("DateUtils.string2Date", var2);
            return null;
        }
    }

    /**
     * 计算两个时间的间隔，单位为天
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int daysBetween(LocalDateTime date1, LocalDateTime date2) {
        long between_days = (date2.toEpochSecond(ZoneOffset.of("+8")) - date1.toEpochSecond(ZoneOffset.of("+8"))) / (3600 * 24);
        return Integer.parseInt(String.valueOf(Math.abs(between_days)));
    }

    /*LocalDateTime相关 END****************************************************************************/

    /*LocalDateTime与Date互相转换 START****************************************************************************/
    public static LocalDateTime date2LocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    /*LocalDateTime与Date互相转换 END****************************************************************************/

    /**
     * 返回日时分秒
     *
     * @param secondMillis
     * @return
     */
    private static String secondToTime(long secondMillis) {
        long second = secondMillis / 1000;
        String DateTimes = null;
        long days = second / (60 * 60 * 24);
        long hours = (second % (60 * 60 * 24)) / (60 * 60);
        long minutes = (second % (60 * 60)) / 60;
        long seconds = second % 60;

        if (days > 0) {
            DateTimes = days + "d " + hours + "h " + minutes + "m " + seconds + "s";
        } else if (hours > 0) {
            DateTimes = hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            DateTimes = minutes + "m " + seconds + "s";
        } else {
            DateTimes = seconds + "s";
        }

        return DateTimes;
    }

    /**
     * JAVA计算两个日期相差多少天(by Date String with format "yyyy-MM-dd")
     */
    public synchronized static int daysBetween(String shoetDateStr1, String shoetDateStr2) {
        try {
            Date date1 = getSdfShort().parse(shoetDateStr1);
            Date date2 = getSdfShort().parse(shoetDateStr2);
            int a = (int) ((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));
            return a;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private static final long DAY_MILLISECOND = 1000 * 60 * 60 * 24L;
    private static final long HOUR_MILLISECOND = 1000 * 60 * 60L;
    private static final long MIN_MILLISECOND = 1000 * 60L;
    private static final long SEC_MILLISECOND = 1000L;


    /**
     * 转换成字符串，格式：XX天XX小时XX分XX秒
     *
     * @param haveTime 时间差
     * @return
     */
    private static String getTimeBetweenStr(long haveTime) {
        int day = 0, hour = 0, min = 0, second = 0;
        if (haveTime > DAY_MILLISECOND) {
            day = (int) (haveTime / DAY_MILLISECOND);
            haveTime -= day * DAY_MILLISECOND;
        }
        if (haveTime > HOUR_MILLISECOND) {
            hour = (int) (haveTime / HOUR_MILLISECOND);
            haveTime -= hour * HOUR_MILLISECOND;
        }
        if (haveTime > MIN_MILLISECOND) {
            min = (int) (haveTime / MIN_MILLISECOND);
            haveTime -= min * MIN_MILLISECOND;
        }
        if (haveTime > SEC_MILLISECOND) {
            second = (int) (haveTime / SEC_MILLISECOND);
        }
        String result = (day > 0 ? day + "天" : "") + (hour > 0 ? hour + "小时" : "") + (min > 0 ? min + "分" : "") + (second > 0 ? second + "秒" : "");
        return result;
    }


    public static String convertTimestampToDHMS(long timestamp) {
        if (timestamp == 0) {
            return "0s";
        }

        long seconds = timestamp / 1000;
        long minutes = (seconds % 3600) / 60;
        long hours = (seconds % 86400) / 3600;
        long days = seconds / 86400;
        long remainingSeconds = seconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append("day");
        }
        if (hours > 0) {
            result.append(hours).append("h");
        }
        if (minutes > 0) {
            result.append(minutes).append("min");
        }
        if (remainingSeconds > 0) {
            result.append(remainingSeconds).append("s");
        }
        return result.toString();
    }

}
