package com.dmitry.baranovsky.serverstatecommandprocessor.commands;

import com.dmitry.baranovsky.serverstatecommandprocessor.Command;
import com.dmitry.baranovsky.serverstatecommandprocessor.CommandModule;
import com.dmitry.baranovsky.serverstatecommandprocessor.Result;
import com.dmitry.baranovsky.serverstatecommandprocessor.ServerStateCommandProcessor;
import com.google.common.eventbus.Subscribe;

/**
 * Implementation of the [24h] command.
 * Always returns ON.
 */
@CommandModule
public class AlwaysOnCommand extends Command {

    /**
     * Standard AlwaysOnCommand constructor. Requires the current command processor instance.
     *
     * @param processor the command processor to be used.
     */
    public AlwaysOnCommand(ServerStateCommandProcessor processor) {
        super(processor);
    }

    @Override
    public Result execute(String argument) {
        if (!argument.equals("24h")) {
            return new Result("Wrong command format: " + argument);
        }
        if (processor.isRunning()) {
            return new Result(ServerStateCommandProcessor.Action.NEUTRAL,
                    "Server is already turned on");
        }
        return new Result(ServerStateCommandProcessor.Action.ON,
                "24h but the server was turned off");
    }

    @Subscribe
    private static void register(ServerStateCommandProcessor processor) {
        processor.register("24h", AlwaysOnCommand.class);
    }
}