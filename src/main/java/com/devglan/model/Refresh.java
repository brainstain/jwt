package com.devglan.model;

import javax.persistence.*;

@Entity
public class Refresh {


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column
    private String username;

    @Column(columnDefinition = "TEXT")
    private String token;

    @Column
    private Boolean valid;

    public Boolean isValidRefresh(){
        return this.valid;
    }

    public void invalidate(){
        this.valid = false;
    }

    public void SetNewRefresh(String token, String username){
        this.valid  = true;
        this.token = token;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}