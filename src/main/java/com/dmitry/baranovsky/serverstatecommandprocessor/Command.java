package com.dmitry.baranovsky.serverstatecommandprocessor;

/**
 * Base Command class
 * All commands must extend this class
 */
public abstract class Command {
    protected final ServerStateCommandProcessor processor;

    public Command(ServerStateCommandProcessor processor){
        this.processor = processor;
    }
    /**
     * Executes the command checking its arguments
     *
     * @param argument String containing the command name and its arguments if any
     * @return a Result enum containing errors if needed
     */
    public abstract Result execute(String argument);
}