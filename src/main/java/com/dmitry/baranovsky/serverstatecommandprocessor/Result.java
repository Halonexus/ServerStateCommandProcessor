package com.dmitry.baranovsky.serverstatecommandprocessor;

import lombok.Getter;

public class Result {
    @Getter
    private ServerStateCommandProcessor.Action action;
    @Getter
    private String reason;
    @Getter
    private final boolean errorFlag;
    @Getter
    private String error;

    public void addError(String newError) {
        error = error + ";\n" + newError;
    }
    public void addReason(String newReason) {
        reason = reason + ";\n" + newReason;
    }

    public Result(ServerStateCommandProcessor.Action action, String reason) {
        this.action = action;
        this.reason = reason;
        errorFlag = false;
    }
    public Result(String error){
        errorFlag = true;
        this.error = error;
        action = ServerStateCommandProcessor.Action.ERROR;
    }
}
