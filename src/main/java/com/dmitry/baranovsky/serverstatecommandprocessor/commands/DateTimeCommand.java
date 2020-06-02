package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.*;
import com.google.common.eventbus.Subscribe;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class of commands that have days and time periods as arguments
 */
@CommandModule
public class DateTimeCommand extends Command {
    private boolean isErrorState = false;
    private String error;
    private boolean isStart = false;
    private boolean isEnd = false;
    private static final String ARGUMENT_SPLIT_PATTERN = "\\s+";
    public static final Map<String, DayOfWeek> DAYS = new HashMap<>();
    private static final String TIME_REGEX =
            "^([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])-([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])$";

    static {
        DAYS.put("Mon", DayOfWeek.MONDAY);
        DAYS.put("Tue", DayOfWeek.TUESDAY);
        DAYS.put("Wed", DayOfWeek.WEDNESDAY);
        DAYS.put("Thu", DayOfWeek.THURSDAY);
        DAYS.put("Fri", DayOfWeek.FRIDAY);
        DAYS.put("Sat", DayOfWeek.SATURDAY);
        DAYS.put("Sun", DayOfWeek.SUNDAY);
    }

    public DateTimeCommand(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        String[] arguments = Utilities.splitArguments(argument, ARGUMENT_SPLIT_PATTERN);

        if ((!isDayOfWeekMatching(arguments) || !isTimeMatching(arguments[arguments.length - 1])) && !isErrorState) {
            return new Result(ServerStateCommandProcessor.Action.NEUTRAL, "Current time outside given work time");
        }
        if (isErrorState) {
            return new Result(error);
        }
        if (isStart) {
            if (processor.isRunning()) {
                return new Result(ServerStateCommandProcessor.Action.SKIP_START,
                        "It is start time but server is already running");
            }
            return new Result(ServerStateCommandProcessor.Action.ON,
                    "It is start time according to given work time");
        }
        if (isEnd) {
            if (processor.isRunning()) {
                return new Result(ServerStateCommandProcessor.Action.OFF,
                        "It is work end time according to given work time");
            }
            return new Result(ServerStateCommandProcessor.Action.SKIP_END,
                    "It is work end time but the server is already off");
        }
        if (processor.isRunning()) {
            return new Result(ServerStateCommandProcessor.Action.WORK_TIME,
                    "It is currently work time and the server is running");
        }
        return new Result(ServerStateCommandProcessor.Action.ON,
                "It is work time but the server was off");
    }

    private boolean isDayOfWeekMatching(String[] arguments) {
        DayOfWeek currentDay = processor.getCurrentDateTime().getDayOfWeek();
        boolean foundMatchingDay = false;
        boolean readingDays = true;
        int i = 0;
        while (readingDays) {
            // For a set of days with whitespace characters eg:'Mon, Tue, Wed'
            if (arguments[i].endsWith(",")) {
                String temp = arguments[i].substring(0, arguments[i].length() - 1);
                if (!DAYS.containsKey(temp)) {
                    isErrorState = true;
                    error = error + "Illegal day of week:" + temp;
                    break;
                } else if (DAYS.get(temp) == currentDay) {
                    foundMatchingDay = true;
                }
            }
            // For a set of days without separating white space characters eg:'Mon,Tue,Wed'
            else if (arguments[i].contains(",")) {
                String[] setOfDays = Utilities.splitArguments(arguments[i], ",");
                for (String day : setOfDays) {
                    if (!DAYS.containsKey(day)) {
                        isErrorState = true;
                        error = error + "Illegal day of week:" + day;
                        break;
                    }
                    if (DAYS.get(day) == processor.getCurrentDateTime().getDayOfWeek()) {
                        foundMatchingDay = true;
                    }
                }
                readingDays = false;
            }
            // For a day period eg:'Mon-Fri'
            else if (arguments[i].contains("-")) {
                String[] dayPeriod = Utilities.splitArguments(arguments[i], "-", 2);
                if (!DAYS.containsKey(dayPeriod[0]) || !DAYS.containsKey(dayPeriod[1])) {
                    isErrorState = true;
                    error = error + "Illegal day of week:" + dayPeriod[0] + "-" + dayPeriod[1];
                    break;
                } else if (DAYS.get(dayPeriod[0]).getValue() <= currentDay.getValue() &&
                        currentDay.getValue() <= DAYS.get(dayPeriod[1]).getValue()) {
                    foundMatchingDay = true;
                    readingDays = false;
                } else {
                    return false;
                }
            }
            // For a single day or the last day of a set eg:'Tue'
            else {
                if (arguments[i].equals("All") ||
                        arguments[i].equals("WD") && currentDay.getValue() < DayOfWeek.SATURDAY.getValue() ||
                        arguments[i].equals("WE") && currentDay.getValue() > DayOfWeek.FRIDAY.getValue()) {
                    foundMatchingDay = true;
                    readingDays = false;
                } else if (arguments[i].equals("WD") || arguments[i].equals("WE")) {
                    readingDays = false;
                } else if (!DAYS.containsKey(arguments[i])) {
                    isErrorState = true;
                    error = error + "Illegal day of week:" + arguments[i];
                    break;
                } else if (DAYS.get(arguments[i]) == currentDay) {
                    foundMatchingDay = true;
                    readingDays = false;
                } else {
                    return false;
                }
            }
            i++;
        }
        return foundMatchingDay;
    }

    private boolean isTimeMatching(String time) {
        if (time.equals("24h")) {
            return true;
        }
        int currentHour = processor.getCurrentDateTime().getHour();
        int currentMinute = processor.getCurrentDateTime().getMinute();
        if (Pattern.matches(TIME_REGEX, time)) {
            Matcher matcher = Pattern.compile(TIME_REGEX).matcher(time);
            while (matcher.find()) {
                if (currentHour == Integer.parseInt(matcher.group(1)) &&
                        currentMinute == Integer.parseInt(matcher.group(2))) {
                    isStart = true;
                } else if (currentHour == Integer.parseInt(matcher.group(3)) &&
                        currentMinute == Integer.parseInt(matcher.group(4))) {
                    isEnd = true;
                }
                if (currentHour > Integer.parseInt(matcher.group(1)) &&
                        currentHour < Integer.parseInt(matcher.group(3)) ||
                        currentHour == Integer.parseInt(matcher.group(1)) &&
                                currentMinute >= Integer.parseInt(matcher.group(2)) ||
                        currentHour == Integer.parseInt(matcher.group(3)) &&
                                currentMinute < Integer.parseInt(matcher.group(4))) {
                    return true;
                }
            }
        } else {
            isErrorState = true;
            error = error + "Wrong time format:" + time;
        }
        return false;
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor) {
        processor.register("All", DateTimeCommand.class);
        processor.register("WD", DateTimeCommand.class);
        processor.register("WE", DateTimeCommand.class);
        processor.register("Mon", DateTimeCommand.class);
        processor.register("Tue", DateTimeCommand.class);
        processor.register("Wed", DateTimeCommand.class);
        processor.register("Thu", DateTimeCommand.class);
        processor.register("Fri", DateTimeCommand.class);
        processor.register("Sat", DateTimeCommand.class);
        processor.register("Sun", DateTimeCommand.class);
    }
}
