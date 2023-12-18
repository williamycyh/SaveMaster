package com.savemaster.savefromfb.db.subscription;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.savemaster.savefromfb.db.BasicDAO;

import io.reactivex.Flowable;

@Dao
public abstract class SubscriptionDAO implements BasicDAO<SubscriptionEntity> {
    @Override
    @Query("SELECT * FROM " + SubscriptionEntity.SUBSCRIPTION_TABLE)
    public abstract Flowable<List<SubscriptionEntity>> getAll();

    @Override
    @Query("DELETE FROM " + SubscriptionEntity.SUBSCRIPTION_TABLE)
    public abstract int deleteAll();

    @Override
    @Query("SELECT * FROM " + SubscriptionEntity.SUBSCRIPTION_TABLE + " WHERE " + SubscriptionEntity.SUBSCRIPTION_SERVICE_ID + " = :serviceId")
    public abstract Flowable<List<SubscriptionEntity>> listByService(int serviceId);

    @Query("SELECT * FROM " + SubscriptionEntity.SUBSCRIPTION_TABLE + " WHERE " +
            SubscriptionEntity.SUBSCRIPTION_URL + " LIKE :url AND " +
            SubscriptionEntity.SUBSCRIPTION_SERVICE_ID + " = :serviceId")
    public abstract Flowable<List<SubscriptionEntity>> getSubscription(int serviceId, String url);

    @Query("SELECT " + SubscriptionEntity.SUBSCRIPTION_UID + " FROM " + SubscriptionEntity.SUBSCRIPTION_TABLE + " WHERE " +
            SubscriptionEntity.SUBSCRIPTION_URL + " LIKE :url AND " +
            SubscriptionEntity.SUBSCRIPTION_SERVICE_ID + " = :serviceId")
    abstract Long getSubscriptionIdInternal(int serviceId, String url);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract Long insertInternal(final SubscriptionEntity entities);

    @Transaction
    public List<SubscriptionEntity> upsertAll(List<SubscriptionEntity> entities) {
        for (SubscriptionEntity entity : entities) {
            Long uid = insertInternal(entity);

            if (uid != -1) {
                entity.setUid(uid);
                continue;
            }

            uid = getSubscriptionIdInternal(entity.getServiceId(), entity.getUrl());
            entity.setUid(uid);

            if (uid == -1) {
                throw new IllegalStateException("Invalid subscription id (-1)");
            }

            update(entity);
        }

        return entities;
    }
}
