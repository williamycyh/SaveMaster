package com.savemaster.savefromfb.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.savemaster.savefromfb.db.stream.dao.StreamDAO;
import com.savemaster.savefromfb.db.stream.dao.StreamStateDAO;
import com.savemaster.savefromfb.db.stream.model.StreamEntity;
import com.savemaster.savefromfb.db.stream.model.StreamStateEntity;
import com.savemaster.savefromfb.db.history.dao.SearchHistoryDAO;
import com.savemaster.savefromfb.db.history.dao.StreamHistoryDAO;
import com.savemaster.savefromfb.db.history.model.SearchHistoryEntry;
import com.savemaster.savefromfb.db.history.model.StreamHistoryEntity;
import com.savemaster.savefromfb.db.playlist.dao.PlaylistDAO;
import com.savemaster.savefromfb.db.playlist.dao.PlaylistRemoteDAO;
import com.savemaster.savefromfb.db.playlist.dao.PlaylistStreamDAO;
import com.savemaster.savefromfb.db.playlist.model.PlaylistEntity;
import com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity;
import com.savemaster.savefromfb.db.playlist.model.PlaylistStreamEntity;
import com.savemaster.savefromfb.db.subscription.SubscriptionDAO;
import com.savemaster.savefromfb.db.subscription.SubscriptionEntity;

@TypeConverters({Converters.class})
@Database(
        entities = {
                SubscriptionEntity.class, SearchHistoryEntry.class,
                StreamEntity.class, StreamHistoryEntity.class, StreamStateEntity.class,
                PlaylistEntity.class, PlaylistStreamEntity.class, PlaylistRemoteEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "gagtube.db";

    public abstract SubscriptionDAO subscriptionDAO();

    public abstract SearchHistoryDAO searchHistoryDAO();

    public abstract StreamDAO streamDAO();

    public abstract StreamHistoryDAO streamHistoryDAO();

    public abstract StreamStateDAO streamStateDAO();

    public abstract PlaylistDAO playlistDAO();

    public abstract PlaylistStreamDAO playlistStreamDAO();

    public abstract PlaylistRemoteDAO playlistRemoteDAO();
}
