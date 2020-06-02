package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.Command;
import com.dmitry.baranovsky.serverstatecommandprocessor.CommandModule;
import com.dmitry.baranovsky.serverstatecommandprocessor.Result;
import com.dmitry.baranovsky.serverstatecommandprocessor.ServerStateCommandProcessor;
import com.google.common.eventbus.Subscribe;

/**
 * Manual command that returns IGNORE_TIME
 */
@CommandModule
public class IgnoreTimeCommand extends Command {

    public IgnoreTimeCommand(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        if (!argument.equals("Manual")) {
            return new Result("Wrong Manual command format: " + argument);
        }
        return new Result(ServerStateCommandProcessor.Action.IGNORE_TIME,
                "");
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor){
        processor.register("Manual", IgnoreTimeCommand.class);
    }
}
