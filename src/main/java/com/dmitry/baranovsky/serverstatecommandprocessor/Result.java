package com.dmitry.baranovsky.serverstatecommandprocessor;

import lombok.Getter;

/**
 * An object containing most of the result information.
 */
public class Result {
    @Getter
    private ServerStateCommandProcessor.Action action;
    @Getter
    private String reason;
    @Getter
    private final boolean errorFlag;
    @Getter
    private String error;

    /**
     * Used for adding a new error message to the result.
     *
     * @param newError the error message to be added to the existing ErrorMessage.
     */
    public void addError(String newError) {
        error = error + ";\n" + newError;
    }

    /**
     * Used for adding a new reason message for an action.
     *
     * @param newReason the reason message to be added.
     */
    public void addReason(String newReason) {
        reason = reason + ";\n" + newReason;
    }

    /**
     * A successful result constructor.
     * Used when no errors occurred, sets ErrorFlag to false and leaves ErrorMessage blank.
     *
     * @param action the action to be performed on the server.
     * @param reason the reason for that action.
     */
    public Result(ServerStateCommandProcessor.Action action, String reason) {
        this.action = action;
        this.reason = reason;
        errorFlag = false;
    }

    /**
     * An error result constructor.
     * Used when an error occurred, sets ErrorFlag to true, may contain multiple error messages.
     *
     * @param error the initial error message.
     */
    public Result(String error) {
        errorFlag = true;
        this.error = error;
        action = ServerStateCommandProcessor.Action.ERROR;
    }
}