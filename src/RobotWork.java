import org.json.JSONObject;

import java.util.Calendar;

public class RobotWork {

    enum PeriodType {
        MORNING, DAY, NIGHT
    }

    class RoboRate {
        int[] mStartDay;
        int[] mEndDay;
        int[] mValue;

        public RoboRate(String startTime, String endTime, int[] value) {
            mStartDay = stringToTime(startTime);
            mEndDay = stringToTime(endTime);
            if (value.length != 4) {
                System.err.println("Warning: Invalid length of robot rate array");
            }
            mValue = value;
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

        if (!standardDay.getString("start").equals(extraDay.getString("start")))
            System.out.println("Warning: The workday start time is not the same as the weekend day");

        if (!standardDay.getString("end").equals(extraDay.getString("end")))
            System.out.println("Warning: The workday End time is not the same as the weekend day");


    }

    public void calculateValue() {
        int workTime = 0;
        value = 0;
        long start = DateTransUtils.timeToStamp(info.mStart);
        long end = DateTransUtils.timeToStamp(info.mEnd);
        Calendar currentTime = Calendar.getInstance();
        /* Assume that the start of the day is the end of the night
         * and the end of the day is the start of the night */
        Calendar nextTime = Calendar.getInstance();
        currentTime.setTimeInMillis(start);
        boolean isDay = true;
        boolean isWorkday = true;
        PeriodType type = PeriodType.MORNING;

        nextTime.setTimeInMillis(currentTime.getTimeInMillis());
        setCalValue(nextTime, roboRate.mStartDay[0], roboRate.mStartDay[1], roboRate.mStartDay[2]);
        if (currentTime.getTimeInMillis() > nextTime.getTimeInMillis()) {
            setCalValue(nextTime, roboRate.mEndDay[0], roboRate.mEndDay[1], roboRate.mEndDay[2]);
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
            System.out.println("Start: " + currentTime.getTime().toString());
            System.out.println("End: " + nextTime.getTime().toString());
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
                        setCalValue(nextTime, roboRate.mEndDay[0], roboRate.mEndDay[1], roboRate.mEndDay[2]);
                        type = PeriodType.DAY;
                        break;
                    case DAY:
                        nextTime.setTimeInMillis(DateTransUtils.getDailyEndTime(nextTime.getTimeInMillis()));
                        type = PeriodType.NIGHT;
                        break;
                    case NIGHT:
                        setCalValue(nextTime, roboRate.mStartDay[0], roboRate.mStartDay[1], roboRate.mStartDay[2]);
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

    public int[] stringToTime(String timeString) {
        String[] split = timeString.split(":");
        int length = split.length;
        if (length != 3) {
            System.err.println("Invalid time input");
            // TODO Error Handling
        }
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }

    public void setCalValue(Calendar cal, int hour, int minute, int second) {
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
    }

    public boolean isDay(Calendar cal) {
        int calTime = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
        int startT = roboRate.mStartDay[0] * 3600 + roboRate.mStartDay[1] * 60 + roboRate.mStartDay[2];
        int endT = roboRate.mEndDay[0] * 3600 + roboRate.mEndDay[1] * 60 + roboRate.mEndDay[2];
        return calTime > startT && calTime < endT;
    }
}
