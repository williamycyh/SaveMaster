package com.savemaster.savefromfb.db.history.dao;

import com.savemaster.savefromfb.db.BasicDAO;

public interface HistoryDAO<T> extends BasicDAO<T> {
    T getLatestEntry();
}
