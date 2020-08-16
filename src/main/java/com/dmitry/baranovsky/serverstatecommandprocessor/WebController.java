package com.dmitry.baranovsky.serverstatecommandprocessor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The main controller.
 * <p>
 * This class is responsible for executing the main functionality of the program.
 * </p>
 */
@RestController
public class WebController {
    /**
     * Handles the /process request.
     * <p>
     * Checks the current time against the provided timetable and server state
     * and returns the required action or an error message.
     * </p>
     *
     * @param input the JSON containing "Work Hours", "Current Time", "Launch Time",
     *              "Time Zone", "Server State", "Patch Time".
     * @return the JSON containing "Action", "Reason", "ErrorFlag", "ErrorMessage", "LocalTime".
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public Map<String, String> process(@RequestBody Map<String, String> input) {
        ServerStateCommandProcessor processor = new ServerStateCommandProcessor();
        return processor.run(input);
    }
}