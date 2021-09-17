import java.util.Calendar;

public class Test {
    public static void main(String[] args) {
        long time = DateTransUtils.timeToStamp("2038-01-01T20:15:00");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        System.out.println(DateTransUtils.isWeekday(cal));
    }
}
