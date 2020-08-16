package com.dmitry.baranovsky.serverstatecommandprocessor;

import com.dmitry.baranovsky.serverstatecommandprocessor.commands.DateTimeCommand;
import com.google.common.eventbus.EventBus;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.Annotation;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class for processing the work time commands.
 * <p>
 * Registers all commands and then processes given work time strings.
 */
@SpringBootApplication
@SuppressWarnings("UnstableApiUsage")
public class ServerStateCommandProcessor {
    private static final String COMMAND_PACKAGE = "com.dmitry.baranovsky.serverstatecommandprocessor.commands";
    private static final String COMMAND_SPLIT_PATTERN = "]";
    private static final String ARGUMENT_SPLIT_PATTERN = "[\\s+,-]";
    private static final String SERVER_STATE_REGEX = "^(running|stopped)$";
    private static final String TIME_REGEX = "^(\\d+)/(\\d+)/(\\d+)" +
            " ([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
    private static final String PATCH_TIME_REGEX =
            "^\\[([1-5]) (Mon|Tue|Wed|Thu|Fri|Sat|Sun)" +
                    " ([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])\\]$";
    private static final ZoneId UTC = ZoneId.of("UTC");
    private Map<String, Class<?>> PRIMARY_MAP = new HashMap<>();
    private Map<String, Class<?>> CONFIG_MAP = new HashMap<>();
    private boolean isRunning;
    @Getter
    private ZonedDateTime currentDateTime;
    @Setter
    @Getter
    private int UTCshift = 0;
    @Getter
    private ZonedDateTime launchDateTime;
    private ZoneId localTimeZone;
    private ZonedDateTime localDateTime;
    private String patchTime;
    private String workTime;
    private String error;

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * The command processor constructor.
     * Registers all commands.
     */
    public ServerStateCommandProcessor() {
        Set<Class<?>> commands = lookForAnnotatedOn(CommandModule.class);
        EventBus eventBus = new EventBus();
        for (Class<?> aClass : commands) {
            try {
                eventBus.register(aClass.getDeclaredConstructor(ServerStateCommandProcessor.class).newInstance(this));
            } catch (Exception e) {
                error = "Couldn't find public constructor for command class: " + aClass.getName();
            }
        }
        eventBus.post(this);
    }

    /**
     * Registers a timetable command in this processor.
     *
     * @param key         the command key as seen in work time strings.
     * @param commandType the command implementation class.
     */
    public void register(String key, Class<?> commandType) {
        PRIMARY_MAP.put(key, commandType);
    }

    /**
     * Registers a configuration command in this processor.
     *
     * @param key         the command key as seen in work time strings.
     * @param commandType the command implementation class.
     */
    public void registerConfig(String key, Class<?> commandType) {
        CONFIG_MAP.put(key, commandType);
    }

    private Set<Class<?>> lookForAnnotatedOn(Class<? extends Annotation> annotation) {
        return new ClassGraph()
                .whitelistPackages(COMMAND_PACKAGE)
                .enableAnnotationInfo()
                .scan()
                .getAllClasses().stream().filter(classInfo -> classInfo.hasAnnotation(annotation.getName())).map(ClassInfo::loadClass)
                .collect(Collectors.toSet());
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerStateCommandProcessor.class, args);
    }

    /**
     * Processes the given workTime String using provided time arguments.
     *
     * @return the JSON represented as a Map<\String, String>.
     */
    public Map<String, String> run(Map<String, String> inputJSON) {
        //reading input
        if (!readInput(inputJSON)) {
            return returnJSONString(error);
        }
        //Command processing
        Result[] result = processCommands(Utilities.splitArguments(workTime, COMMAND_SPLIT_PATTERN));
        if (result == null) {
            return returnJSONString(error);
        }
        //result calculation
        return calculateResult(result);
    }

    private boolean readInput(Map<String, String> input) {
        String workTime = input.get("Work Hours");
        String currentTime = input.get("Current Time");
        String launchTime = input.get("Launch Time");
        String timeZone = input.get("Time Zone");
        String serverState = input.get("Server State");
        String patchTime = input.get("Patch Time");

        if (workTime.isEmpty() || launchTime.isEmpty() || timeZone.isEmpty() || serverState.isEmpty()) {
            error = "Null argument";
            return false;
        }
        this.workTime = workTime;
        try {
            localTimeZone = ZoneId.of(timeZone);
        } catch (Exception e) {
            error = "Incorrect time zone:" + timeZone;
            return false;
        }
        if (!Pattern.matches(SERVER_STATE_REGEX, serverState)) {
            error = "Incorrect server state";
            return false;
        } else {
            Matcher matcher = Pattern.compile(SERVER_STATE_REGEX).matcher(serverState);
            if (matcher.matches()) {
                isRunning = matcher.group(1).equals("running");
            }
        }
        if (!Pattern.matches(PATCH_TIME_REGEX, patchTime) && !patchTime.isEmpty()) {
            error = "Incorrect patch time";
            return false;
        }
        this.patchTime = patchTime;
        if (!Pattern.matches(TIME_REGEX, launchTime)) {
            error = "Incorrect launch time: " + launchTime;
            return false;
        } else {
            Matcher matcher = Pattern.compile(TIME_REGEX).matcher(launchTime);
            if (matcher.matches()) {
                int[] matches = new int[6];
                for (int i = 0; i < 6; i++) {
                    matches[i] = Integer.parseInt(matcher.group(1 + i));
                }
                try {
                    launchDateTime = ZonedDateTime.of(matches[2], matches[0], matches[1],
                            matches[3], matches[4], matches[5], 0, UTC);
                } catch (DateTimeException e) {
                    error = "Incorrect launch time: " + launchTime;
                    return false;
                }
            }
        }
        if (!Pattern.matches(TIME_REGEX, currentTime)) {
            error = "Incorrect current time: " + currentTime;
            return false;
        } else {
            Matcher matcher = Pattern.compile(TIME_REGEX).matcher(currentTime);//need to check date
            if (matcher.matches()) {
                int[] matches = new int[6];
                for (int i = 0; i < 6; i++) {
                    matches[i] = Integer.parseInt(matcher.group(1 + i));
                }
                try {
                    currentDateTime = ZonedDateTime.of(matches[2], matches[0], matches[1],
                            matches[3], matches[4], matches[5], 0, UTC);
                } catch (DateTimeException e) {
                    error = "Incorrect current time: " + currentTime;
                    return false;
                }
            }
        }
        if (currentDateTime == null || localTimeZone == null) {
            error = "Null main arguments";
            return false;
        }
        localDateTime = currentDateTime.withZoneSameInstant(localTimeZone);
        if (localTimeZone.getRules().isDaylightSavings(currentDateTime.toInstant())) {
            currentDateTime = currentDateTime.plusHours(1);
        }
        return true;
    }

    private Result[] processCommands(String[] commands) {
        //config commands
        for (int i = 0; i < commands.length; i++) {
            if (!commands[i].startsWith("[")) {
                error = "No '[' found at command origin: " + workTime;
                return null;
            }
            String[] command = Utilities.splitArguments(commands[i], ARGUMENT_SPLIT_PATTERN);
            String key = command[0].substring(1);
            if (CONFIG_MAP.containsKey(key)) {
                Result result;
                try {
                    result = ((Command) CONFIG_MAP.get(key).getConstructor(ServerStateCommandProcessor.class)
                            .newInstance(this)).execute(commands[i].substring(1));
                    if (result.getAction() == Action.ERROR) {
                        error = result.getError();
                        return null;
                    }
                } catch (Exception e) {
                    error = "Command instantiation error for command: "
                            + CONFIG_MAP.get(key).getCanonicalName();
                    return null;
                }
                commands[i] = null;
            }
        }
        currentDateTime = currentDateTime.plusHours(UTCshift);
        //primary commands
        Result[] result = new Result[commands.length];
        for (int i = 0; i < commands.length; i++) {
            if (commands[i] == null) {
                continue;
            }
            String[] command = Utilities.splitArguments(commands[i], ARGUMENT_SPLIT_PATTERN);
            String key = command[0].substring(1);
            if (!PRIMARY_MAP.containsKey(key)) {
                error = "Illegal command: " + key;
                return null;
            }
            try {
                result[i] = ((Command) PRIMARY_MAP.get(key).getConstructor(ServerStateCommandProcessor.class)
                        .newInstance(this)).execute(commands[i].substring(1));
                if (result[i].getAction() == Action.ERROR) {
                    error = result[i].getError();
                    return null;
                }
            } catch (Exception e) {
                error = "Command instantiation error for command: "
                        + PRIMARY_MAP.get(key).getCanonicalName();
                return null;
            }
        }
        return result;
    }

    private Map<String, String> calculateResult(Result[] result) {
        boolean ignoreAll = false;
        boolean ignoreTime = false;
        boolean isWorkTime = false;
        Result startSkipped = null;
        Result endSkipped = null;
        Result start = null;
        Result stop = null;
        Result neutral = null;
        for (Result value : result) {
            if (value == null) {
                continue;
            }
            switch (value.getAction()) {
                case IGNORE_ALL: {
                    ignoreAll = true;
                    break;
                }
                case IGNORE_TIME: {
                    ignoreTime = true;
                    break;
                }
                case WORK_TIME: {
                    isWorkTime = true;
                    break;
                }
                case SKIP_START: {
                    startSkipped = value;
                    break;
                }
                case SKIP_END: {
                    endSkipped = value;
                    break;
                }
                case ON: {
                    start = value;
                    break;
                }
                case OFF: {
                    if (stop == null) {
                        stop = value;
                    } else {
                        stop.addReason(value.getReason());
                    }
                    break;
                }
                case NEUTRAL: {
                    neutral = value;
                    break;
                }
                default:
                    break;
            }
        }
        if (ignoreAll) {
            return returnJSONString(Action.IGNORE_ALL, "No action taken due to OFF command");
        } else if (isPatchTime(patchTime)) {
            if (isRunning) {
                return returnJSONString(Action.SKIP_START,
                        "Patch Time but the server was already running");
            }
            return returnJSONString(Action.ON, "Patch Time turning on the server");
        } else if (ignoreTime) {
            return returnJSONString(Action.IGNORE_TIME, "Not Patch Time and Manual command");
        } else if (isWorkTime) {
            return returnJSONString(Action.WORK_TIME, "It is work time and the server is running");
        } else if (startSkipped != null) {
            return returnJSONString(Action.SKIP_START, startSkipped.getReason());
        } else if (endSkipped != null) {
            return returnJSONString(Action.SKIP_END, endSkipped.getReason());
        } else if (start != null) {
            return returnJSONString(Action.ON, start.getReason());
        } else if (stop != null) {
            return returnJSONString(Action.OFF, stop.getReason());
        } else if (neutral != null) {
            return returnJSONString(Action.NEUTRAL, neutral.getReason());
        } else {
            return returnJSONString("Could not calculate result: " + workTime);
        }
    }

    private boolean isPatchTime(String patchTime) {
        if (patchTime.isBlank()) {
            return false;
        }
        Matcher matcher = Pattern.compile(PATCH_TIME_REGEX).matcher(patchTime);
        if (matcher.matches()) {
            int shift = Integer.parseInt(matcher.group(1));
            if (currentDateTime.getDayOfWeek() == DateTimeCommand.DAYS.get(matcher.group(2)) &&
                    currentDateTime.getHour() == Integer.parseInt(matcher.group(3)) &&
                    currentDateTime.getMinute() == Integer.parseInt(matcher.group(4)) &&
                    currentDateTime.getDayOfMonth() - (shift - 1) * 7 > 0 &&
                    currentDateTime.getDayOfMonth() - shift * 7 <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Possible command results and errors.
     */
    public enum Action {
        ON("Start"),
        OFF("Stop"),
        NEUTRAL(""),
        WORK_TIME(""),
        IGNORE_TIME(""),
        IGNORE_ALL(""),
        SKIP_START("Skip Start"),
        SKIP_END("Skip Stop"),
        ERROR("Error");
        public final String name;

        Action(String name) {
            this.name = name;
        }
    }

    private Map<String, String> returnJSONString(String error) {
        Map<String, String> outJSON = new HashMap<>();
        outJSON.put("Action", "");
        outJSON.put("Reason", "");
        outJSON.put("ErrorFlag", "True");
        outJSON.put("ErrorMessage", error);
        outJSON.put("LocalTime", formatLocalTime());
        return outJSON;
    }

    private Map<String, String> returnJSONString(Action action, String reason) {
        Map<String, String> outJSON = new HashMap<>();
        outJSON.put("Action", action.name);
        outJSON.put("Reason", reason);
        outJSON.put("ErrorFlag", "False");
        outJSON.put("ErrorMessage", "");
        outJSON.put("LocalTime", formatLocalTime());
        return outJSON;
    }

    private String formatLocalTime() {
        if (localDateTime != null) {
            return localDateTime.format(DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss"));
        }
        return "error";
    }
}