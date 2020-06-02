package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.Command;
import com.dmitry.baranovsky.serverstatecommandprocessor.CommandModule;
import com.dmitry.baranovsky.serverstatecommandprocessor.Result;
import com.dmitry.baranovsky.serverstatecommandprocessor.ServerStateCommandProcessor;
import com.google.common.eventbus.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandModule
public class UTCOffset extends Command {
    private static final String UTC_REGEX = "^UTC([+\\-])(\\d+)$";

    public UTCOffset(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        Matcher matcher = Pattern.compile(UTC_REGEX).matcher(argument);
        if (matcher.find()) {
            if (!matcher.matches()) {
                return new Result("Wrong UTC command format: " + argument);
            }
            if (processor.getUTCshift() != 0) {
                return new Result("Multiple UTC zone arguments");
            }
            if (matcher.group(1).charAt(0) == '+') {
                processor.setUTCshift(Integer.parseInt(matcher.group(2)));
            }
            processor.setUTCshift(-Integer.parseInt(matcher.group(2)));
            return new Result(ServerStateCommandProcessor.Action.NEUTRAL, "");
        }
        return new Result("Wrong UTC command format: " + argument);
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor){
        processor.registerConfig("UTC", UTCOffset.class);
    }
}
