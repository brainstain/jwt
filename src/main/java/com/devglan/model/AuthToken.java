package com.devglan.model;

public class AuthToken {

    private String token;
    private String refresh;

    public AuthToken(){

    }

    public AuthToken(String token, String refresh){
        this.token = token;
        this.refresh = refresh;
    }

    public String getToken() {
        return token;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRefresh(String refresh){
        this.refresh = refresh;
    }
}
