package com.devglan.dao;

import com.devglan.model.Refresh;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshDao extends CrudRepository<Refresh, Long> {
    Refresh findByUsername(String username);
    Refresh findByToken(String token);
    void deleteByUsername(String username);
}
