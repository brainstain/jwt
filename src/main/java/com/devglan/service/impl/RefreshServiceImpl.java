package com.devglan.service.impl;

import javax.transaction.Transactional;

import com.devglan.dao.RefreshDao;
import com.devglan.model.Refresh;
import com.devglan.service.RefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "refreshService")
public class RefreshServiceImpl implements RefreshService {

    @Autowired
    private RefreshDao refreshDao;

    @Override
    public Refresh getRefreshByName(String name) {
        return refreshDao.findByUsername(name);
    }

    @Override
    public Refresh save(Refresh refresh) {
        return refreshDao.save(refresh);
    }

    @Override
    public void invalidate(Refresh refresh) {
        refresh.invalidate();
        refreshDao.save(refresh);
    }

    @Override
    public Refresh getRefreshByToken(String token) {
        return refreshDao.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        refreshDao.deleteByUsername(username);
    }
}
