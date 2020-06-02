package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.Command;
import com.dmitry.baranovsky.serverstatecommandprocessor.CommandModule;
import com.dmitry.baranovsky.serverstatecommandprocessor.Result;
import com.dmitry.baranovsky.serverstatecommandprocessor.ServerStateCommandProcessor;
import com.google.common.eventbus.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UOD command that returns OFF when the current time is the same as the specified time
 */
@CommandModule
public class UpOnDemandCommand extends Command {
    private static final String UOD_REGEX = "^UOD ([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])$";

    public UpOnDemandCommand(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        Matcher matcher = Pattern.compile(UOD_REGEX).matcher(argument);
        while (matcher.find()) {
            if (!matcher.matches()) {
                return new Result("Wrong UOD command format: " + argument);
            }
            if (processor.getCurrentDateTime().getHour() == Integer.parseInt(matcher.group(1)) &&
                    processor.getCurrentDateTime().getMinute() == Integer.parseInt(matcher.group(2))) {
                if (processor.isRunning()) {
                    return new Result(ServerStateCommandProcessor.Action.OFF, "up on demand shutdown time");
                }
                return new Result(ServerStateCommandProcessor.Action.NEUTRAL,
                        "Up on demand shutdown time but the server is already off");
            }
        }
        return new Result(ServerStateCommandProcessor.Action.NEUTRAL, "Up on demand time is not reached");
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor){
        processor.register("UOD", UpOnDemandCommand.class);
    }
}
