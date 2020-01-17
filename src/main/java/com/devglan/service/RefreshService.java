package com.devglan.service;

import com.devglan.model.Refresh;

public interface RefreshService {
    Refresh getRefreshByName(String name);
    Refresh getRefreshByToken(String token);
    Refresh save(Refresh refresh);
    void invalidate(Refresh refresh);
    void deleteByUsername(String username);
}