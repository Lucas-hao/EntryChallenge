import org.json.JSONObject;

import java.util.Calendar;

public class RobotWork {

    enum PeriodType {
        MORNING, DAY, NIGHT
    }

    static class Time {
        int hour;
        int min;
        int sec;

        public Time(int h, int m, int s) {
            hour = h;
            min = m;
            sec = s;
        }

        public static Time stringToTime(String timeString) {
            String[] split = timeString.split(":");
            int length = split.length;
            if (length != 3) {
                System.err.println("Invalid time input");
                // TODO Error Handling
            }
            return new Time(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }
    }

    static class RoboRate {
        Time mStartDay;
        Time mEndDay;
        int[] mValue;

        public RoboRate(String stdStart, String stdEnd, int[] value) {
            mStartDay = Time.stringToTime(stdStart);
            mEndDay = Time.stringToTime(stdEnd);
            if (value.length != 4) {
                System.err.println("Warning: Invalid length of robot rate array");
                mValue = new int []{0, 0, 0, 0};
            } else {
                mValue = value;
            }
        }
    }

    static class Shift {
        String mStart;
        String mEnd;

        public Shift(String start, String end) {
            mStart = start;
            mEnd = end;
        }
    }

    private int value;
    private Shift info;
    private RoboRate roboRate;

    RobotWork(String input) {
        value = 0;
        initializeData(input);
    }

    public void initializeData(String input) {
        JSONObject inputJSON = new JSONObject(input);

        JSONObject shiftJSON = inputJSON.getJSONObject("shift");
        info = new Shift(shiftJSON.getString("start"), shiftJSON.getString("end"));

        JSONObject roboRateJSON = inputJSON.getJSONObject("roboRate");

        JSONObject standardDay = roboRateJSON.getJSONObject("standardDay");
        JSONObject standardNight = roboRateJSON.getJSONObject("standardNight");
        JSONObject extraDay = roboRateJSON.getJSONObject("extraDay");
        JSONObject extraNight = roboRateJSON.getJSONObject("extraNight");

        roboRate = new RoboRate(standardDay.getString("start"), standardDay.getString("end"),
                new int[]{standardDay.getInt("value"), standardNight.getInt("value"),
                        extraDay.getInt("value"), extraNight.getInt("value")});


        // Assume day end time is the same as the night start time
        if (!standardDay.getString("end").equals(standardNight.getString("start"))
                || !extraDay.getString("end").equals(extraNight.getString("start"))) {
            System.err.println("Warning: Day end time is different from the night start time");
        }

        // and night end time is the same as the day start time
        if (!standardDay.getString("start").equals(standardNight.getString("end"))
                || !extraDay.getString("start").equals(extraNight.getString("end"))) {
            System.err.println("Warning: Night end time is different from the day start time");
        }

        /*
        Assume the standard day start time is the same as the extra night end time
        and the standard night end time is the same as the extra day start time,
        So the day start time and the day end time should all be the same
         */
        if (!standardDay.getString("start").equals(extraDay.getString("start"))
                || !standardDay.getString("end").equals(extraDay.getString("end"))) {
            System.err.println("Warning: Standard start time and end time is different from that of extraDay");
        }
    }

    public void calculateValue() {
        int workTime = 0;
        value = 0;
        long start = DateTransUtils.timeToStamp(info.mStart);
        long end = DateTransUtils.timeToStamp(info.mEnd);
        Calendar currentTime = Calendar.getInstance();
        Calendar nextTime = Calendar.getInstance();
        currentTime.setTimeInMillis(start);
        boolean isDay;
        boolean isWorkday;
        PeriodType type = PeriodType.MORNING;

        nextTime.setTimeInMillis(currentTime.getTimeInMillis());
        setCalValue(nextTime, roboRate.mStartDay.hour, roboRate.mStartDay.min, roboRate.mStartDay.sec);
        if (currentTime.getTimeInMillis() > nextTime.getTimeInMillis()) {
            setCalValue(nextTime, roboRate.mEndDay.hour, roboRate.mEndDay.min, roboRate.mEndDay.sec);
            if (currentTime.getTimeInMillis() > nextTime.getTimeInMillis()) {
                nextTime.setTimeInMillis(DateTransUtils.getDailyEndTime(nextTime.getTimeInMillis()));
                type = PeriodType.NIGHT;
            } else {
                type = PeriodType.DAY;
            }
        }

        if (nextTime.getTimeInMillis() >= end) {
            nextTime.setTimeInMillis(end);
        }

        while (currentTime.getTimeInMillis() < end) {
            System.out.println("Start: " + currentTime.getTime());
            System.out.println("End: " + nextTime.getTime());
            int currentValue;
            isWorkday = DateTransUtils.isWeekday(currentTime);
            isDay = (type == PeriodType.DAY);
            if (isWorkday && isDay) {
                currentValue = roboRate.mValue[0]; // standard day
            } else if (isWorkday && !isDay) {
                currentValue = roboRate.mValue[1]; // standard night
            } else if (!isWorkday && isDay) {
                currentValue = roboRate.mValue[2]; // extra day
            } else if (!isWorkday && !isDay) {
                currentValue = roboRate.mValue[3]; // extra night
            } else {
                System.out.println("Warning: Invalid State");
                currentValue = 0;
            }
            if (currentTime.getTimeInMillis() + 8 * 60 * 60 * 1000 - workTime <= nextTime.getTimeInMillis()) {
                value += (currentValue * (8 * 60 * 60 * 1000 - workTime) / 60000);
                currentTime.setTimeInMillis(currentTime.getTimeInMillis() + 9 * 60 * 60 * 1000 - workTime);
                workTime = 0;
            } else {
                workTime += nextTime.getTimeInMillis() - currentTime.getTimeInMillis();
                value += (currentValue * (nextTime.getTimeInMillis() - currentTime.getTimeInMillis()) / 60000);
                currentTime.setTimeInMillis(nextTime.getTimeInMillis());
            }

            if (currentTime.getTimeInMillis() >= nextTime.getTimeInMillis()) {
                switch (type) {
                    case MORNING:
                        setCalValue(nextTime, roboRate.mEndDay.hour, roboRate.mEndDay.min, roboRate.mEndDay.sec);
                        type = PeriodType.DAY;
                        break;
                    case DAY:
                        nextTime.setTimeInMillis(DateTransUtils.getDailyEndTime(nextTime.getTimeInMillis()));
                        type = PeriodType.NIGHT;
                        break;
                    case NIGHT:
                        setCalValue(nextTime, roboRate.mStartDay.hour, roboRate.mStartDay.min, roboRate.mStartDay.sec);
                        type = PeriodType.MORNING;
                        break;
                }
            }

            if (nextTime.getTimeInMillis() >= end) {
                nextTime.setTimeInMillis(end);
            }

            System.out.println("Value: " + value);
        }
    }

    public int getValue() {
        return value;
    }

    public void setCalValue(Calendar cal, int hour, int minute, int second) {
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
    }
}
