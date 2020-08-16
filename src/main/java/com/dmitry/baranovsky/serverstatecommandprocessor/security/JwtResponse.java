package com.dmitry.baranovsky.serverstatecommandprocessor.security;

import java.io.Serializable;

/**
 * Class that represents the output JSON containing the generated token.
 */
public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;

    public JwtResponse(String jwttoken) {
        this.jwttoken = jwttoken;
    }

    public String getToken() {
        return this.jwttoken;
    }
}