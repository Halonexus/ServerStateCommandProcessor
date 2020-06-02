package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.Command;
import com.dmitry.baranovsky.serverstatecommandprocessor.CommandModule;
import com.dmitry.baranovsky.serverstatecommandprocessor.Result;
import com.dmitry.baranovsky.serverstatecommandprocessor.ServerStateCommandProcessor;
import com.google.common.eventbus.Subscribe;

/**
 * OFF command that returns IGNORE_ALL
 */
@CommandModule
public class IgnoreAllCommand extends Command {

    public IgnoreAllCommand(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        if (!argument.equals("OFF")) {
            return new Result("Wrong command format: " + argument);
        }
        return new Result(ServerStateCommandProcessor.Action.IGNORE_ALL, "");
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor){
        processor.register("OFF", IgnoreAllCommand.class);
    }
}