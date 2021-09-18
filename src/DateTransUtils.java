

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class DateTransUtils {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);

    public static String stampToTime(long stamp) {
        return timeFormat.format(stamp);
    }

    public static long timeToStamp(String time) {
        try {
            return Objects.requireNonNull(timeFormat.parse(time)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getDailyStartTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getDailyEndTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Calendar getDailySetCalendar(long timeStamp, int hour, int min, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Boolean isWeekday(Calendar cal) {
        return cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY;
    }
}