package com.dmitry.baranovsky.serverstatecommandprocessor;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class Tests {
    @Test
    void splitTest() {
        String a = "[asd][qwe]";
        String[] res = Utilities.splitArguments(a, "]");
        String[] chk = {"[asd", "[qwe"};
        assertArrayEquals(chk, res);
    }

    @Test
    void processorTest() {
        Map<String, String> json = new HashMap<>();
        json.put("Current Time", "05/11/2020 03:00:00");
        json.put("Work Hours", "[Sun 20:00-23:59][WD 00:00-23:55][Sat 00:00-00:55][OSH 4][UTC-3]");
        json.put("Patch Time", "[3 Sat 01:00]");
        json.put("Time Zone", "UTC-3");
        json.put("Server State", "stopped");
        json.put("Launch Time", "05/02/2020 20:00:48");

        Map<String, String> expected = new HashMap<>();
        expected.put("Action", "Start");
        expected.put("ErrorFlag", "False");
        expected.put("LocalTime", "05/11/2020 00:00:00");
        expected.put("ErrorMessage", "");
        expected.put("Reason", "It is start time according to given work time");

        ServerStateCommandProcessor processor = new ServerStateCommandProcessor();
        Map<String, String> result = processor.run(json);
        assertTrue("Maps are equal", Maps.difference(expected, result).areEqual());
    }
}