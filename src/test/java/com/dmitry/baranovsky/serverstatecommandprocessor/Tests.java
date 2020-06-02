package com.dmitry.baranovsky.serverstatecommandprocessor;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Tests {
    @Test
    void splitTest() {
        String a = "[asd][qwe]";
        String[] res = Utilities.splitArguments(a, "]");
        String[] chk = {"[asd", "[qwe"};
        assertArrayEquals(chk, res);
    }

    @Test
    void fullTest1() {
        JSONObject json = new JSONObject();
        json.put("Current Time", "05/11/2020 03:00:00");
        json.put("Work Hours", "[Sun 20:00-23:59][WD 00:00-23:55][Sat 00:00-00:55][OSH 4][UTC-3]");
        json.put("Patch Time", "[3 Sat 01:00]");
        json.put("Time Zone", "UTC-3");
        json.put("Server State", "running");
        json.put("Launch Time", "05/02/2020 20:00:48");
        String input = json.toString();

        ServerStateCommandProcessor a = new ServerStateCommandProcessor();
        String output = a.run(input);
        assertEquals("", output);
    }
}