package com.nokia.as.connector;

public class Authentication {

    private String login;
    private String token;

    public Authentication(String login, String token) {
        this.login = login;
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public String getToken() {
        return token;
    }
}
