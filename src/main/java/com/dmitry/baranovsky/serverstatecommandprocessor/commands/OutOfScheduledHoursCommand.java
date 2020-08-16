package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.Command;
import com.dmitry.baranovsky.serverstatecommandprocessor.CommandModule;
import com.dmitry.baranovsky.serverstatecommandprocessor.Result;
import com.dmitry.baranovsky.serverstatecommandprocessor.ServerStateCommandProcessor;
import com.google.common.eventbus.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the [OSH] command. Returns OFF when a given amount of hours passes after launchTime.
 */
@CommandModule
public class OutOfScheduledHoursCommand extends Command {
    private static final String OSH_REGEX = "^OSH ([0-9])$";

    /**
     * Standard OutOfScheduledHoursCommand constructor. Requires the current command processor instance.
     *
     * @param processor the command processor to be used.
     */
    public OutOfScheduledHoursCommand(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        Matcher matcher = Pattern.compile(OSH_REGEX).matcher(argument);
        while (matcher.find()) {
            if (!matcher.matches()) {
                return new Result("Wrong OSH command format: " + argument);
            }
            if (processor.getCurrentDateTime().minusHours(Integer.parseInt(matcher.group(1)))
                    .isAfter(processor.getLaunchDateTime())) {
                if (processor.isRunning()) {
                    return new Result(ServerStateCommandProcessor.Action.OFF, "Out of scheduled hours");
                }
                return new Result(ServerStateCommandProcessor.Action.NEUTRAL,
                        "Out of scheduled hours but server is already off");
            }
        }
        return new Result(ServerStateCommandProcessor.Action.NEUTRAL,
                "Withing work hours or the time limit hasnt been reached");
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor) {
        processor.register("OSH", OutOfScheduledHoursCommand.class);
    }
}