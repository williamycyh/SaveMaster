package com.savemaster.savefromfb.local.history;

import android.content.Context;
import android.content.SharedPreferences;

import savemaster.save.master.pipd.InfoItem;
import savemaster.save.master.pipd.stream.StreamInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.savemaster.savefromfb.db.AppDatabase;
import com.savemaster.savefromfb.db.GAGTubeDatabase;
import com.savemaster.savefromfb.db.history.dao.SearchHistoryDAO;
import com.savemaster.savefromfb.db.history.dao.StreamHistoryDAO;
import com.savemaster.savefromfb.db.history.model.SearchHistoryEntry;
import com.savemaster.savefromfb.db.history.model.StreamHistoryEntity;
import com.savemaster.savefromfb.db.history.model.StreamHistoryEntry;
import com.savemaster.savefromfb.db.stream.StreamStatisticsEntry;
import com.savemaster.savefromfb.db.stream.dao.StreamDAO;
import com.savemaster.savefromfb.db.stream.dao.StreamStateDAO;
import com.savemaster.savefromfb.db.stream.model.StreamEntity;
import com.savemaster.savefromfb.db.stream.model.StreamStateEntity;

import com.savemaster.savefromfb.R;

import com.savemaster.savefromfb.player.playqueue.PlayQueueItem;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class HistoryRecordManager {
	
	private final AppDatabase database;
	private final StreamDAO streamTable;
	private final StreamHistoryDAO streamHistoryTable;
	private final SearchHistoryDAO searchHistoryTable;
	private final StreamStateDAO streamStateTable;
	private final SharedPreferences sharedPreferences;
	private final String searchHistoryKey;
	private final String streamHistoryKey;
	
	public HistoryRecordManager(final Context context) {
		
		database = GAGTubeDatabase.getInstance(context);
		streamTable = database.streamDAO();
		streamHistoryTable = database.streamHistoryDAO();
		searchHistoryTable = database.searchHistoryDAO();
		streamStateTable = database.streamStateDAO();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		searchHistoryKey = context.getString(R.string.enable_search_history_key);
		streamHistoryKey = context.getString(R.string.enable_watch_history_key);
	}
	
	public Maybe<Long> onViewed(final StreamInfo info) {
		
		if (!isStreamHistoryEnabled()) return Maybe.empty();
		
		final Date currentTime = new Date();
		return Maybe.fromCallable(() -> database.runInTransaction(() -> {
			
			final long streamId = streamTable.upsert(new StreamEntity(info));
			StreamHistoryEntity latestEntry = streamHistoryTable.getLatestEntry();
			
			if (latestEntry != null && latestEntry.getStreamUid() == streamId) {
				streamHistoryTable.delete(latestEntry);
				latestEntry.setAccessDate(currentTime);
				latestEntry.setRepeatCount(latestEntry.getRepeatCount() + 1);
				return streamHistoryTable.insert(latestEntry);
			}
			else {
				return streamHistoryTable.insert(new StreamHistoryEntity(streamId, currentTime));
			}
		})).subscribeOn(Schedulers.io());
	}
	
	Single<Integer> deleteStreamHistory(final long streamId) {
		return Single.fromCallable(() -> streamHistoryTable.deleteStreamHistory(streamId)).subscribeOn(Schedulers.io());
	}
	
	public Single<Integer> deleteWholeStreamHistory() {
		return Single.fromCallable(streamHistoryTable::deleteAll).subscribeOn(Schedulers.io());
	}
	
	public Flowable<List<StreamHistoryEntry>> getStreamHistory() {
		return streamHistoryTable.getHistory().subscribeOn(Schedulers.io());
	}
	
	public Flowable<List<StreamStatisticsEntry>> getStreamStatistics() {
		return streamHistoryTable.getStatistics().subscribeOn(Schedulers.io());
	}
	
	public Single<List<Long>> insertStreamHistory(final Collection<StreamHistoryEntry> entries) {
		
		List<StreamHistoryEntity> entities = new ArrayList<>(entries.size());
		for (final StreamHistoryEntry entry : entries) {
			entities.add(entry.toStreamHistoryEntity());
		}
		return Single.fromCallable(() -> streamHistoryTable.insertAll(entities))
				.subscribeOn(Schedulers.io());
	}
	
	public Single<Integer> deleteStreamHistory(final Collection<StreamHistoryEntry> entries) {
		
		List<StreamHistoryEntity> entities = new ArrayList<>(entries.size());
		for (final StreamHistoryEntry entry : entries) {
			entities.add(entry.toStreamHistoryEntity());
		}
		return Single.fromCallable(() -> streamHistoryTable.delete(entities))
				.subscribeOn(Schedulers.io());
	}
	
	private boolean isStreamHistoryEnabled() {
		return sharedPreferences.getBoolean(streamHistoryKey, false);
	}
	
	public Maybe<Long> onSearched(final int serviceId, final String search) {
		
		if (!isSearchHistoryEnabled()) return Maybe.empty();
		
		final Date currentTime = new Date();
		final SearchHistoryEntry newEntry = new SearchHistoryEntry(currentTime, serviceId, search);
		
		return Maybe.fromCallable(() -> database.runInTransaction(() -> {
			
			SearchHistoryEntry latestEntry = searchHistoryTable.getLatestEntry();
			if (latestEntry != null && latestEntry.hasEqualValues(newEntry)) {
				latestEntry.setCreationDate(currentTime);
				return (long) searchHistoryTable.update(latestEntry);
			}
			else {
				return searchHistoryTable.insert(newEntry);
			}
		})).subscribeOn(Schedulers.io());
	}
	
	public Single<Integer> deleteSearchHistory(final String search) {
		return Single.fromCallable(() -> searchHistoryTable.deleteAllWhereQuery(search)).subscribeOn(Schedulers.io());
	}
	
	public Single<Integer> deleteWholeSearchHistory() {
		return Single.fromCallable(searchHistoryTable::deleteAll).subscribeOn(Schedulers.io());
	}
	
	public Flowable<List<SearchHistoryEntry>> getRelatedSearches(final String query, final int similarQueryLimit, final int uniqueQueryLimit) {
		return query.length() > 0 ? searchHistoryTable.getSimilarEntries(query, similarQueryLimit) : searchHistoryTable.getUniqueEntries(uniqueQueryLimit);
	}
	
	private boolean isSearchHistoryEnabled() {
		return sharedPreferences.getBoolean(searchHistoryKey, false);
	}
	
	public Single<Integer> removeOrphanedRecords() {
		return Single.fromCallable(streamTable::deleteOrphans).subscribeOn(Schedulers.io());
	}
	
	public Maybe<StreamStateEntity> loadStreamState(final PlayQueueItem queueItem) {
		return queueItem.getStream()
				.map((info) -> streamTable.upsert(new StreamEntity(info)))
				.flatMapPublisher(streamStateTable::getState)
				.firstElement()
				.flatMap(list -> list.isEmpty() ? Maybe.empty() : Maybe.just(list.get(0)))
				.filter(state -> state.isValid((int) queueItem.getDuration()))
				.subscribeOn(Schedulers.io());
	}
	
	public Maybe<StreamStateEntity> loadStreamState(final StreamInfo info) {
		return Single.fromCallable(() -> streamTable.upsert(new StreamEntity(info)))
				.flatMapPublisher(streamStateTable::getState)
				.firstElement()
				.flatMap(list -> list.isEmpty() ? Maybe.empty() : Maybe.just(list.get(0)))
				.filter(state -> state.isValid((int) info.getDuration()))
				.subscribeOn(Schedulers.io());
	}
	
	public Completable saveStreamState(@NonNull final StreamInfo info, final long progressTime) {
		return Completable.fromAction(() -> database.runInTransaction(() -> {
			final long streamId = streamTable.upsert(new StreamEntity(info));
			final StreamStateEntity state = new StreamStateEntity(streamId, progressTime);
			if (state.isValid((int) info.getDuration())) {
				streamStateTable.upsert(state);
			} else {
				streamStateTable.deleteState(streamId);
			}
		})).subscribeOn(Schedulers.io());
	}
	
	public Single<StreamStateEntity[]> loadStreamState(final InfoItem info) {
		return Single.fromCallable(() -> {
			final List<StreamEntity> entities = streamTable.getStream(info.getServiceId(), info.getUrl()).blockingFirst();
			if (entities.isEmpty()) {
				return new StreamStateEntity[]{null};
			}
			final List<StreamStateEntity> states = streamStateTable.getState(entities.get(0).getUid()).blockingFirst();
			if (states.isEmpty()) {
				return new StreamStateEntity[]{null};
			}
			return new StreamStateEntity[]{states.get(0)};
		}).subscribeOn(Schedulers.io());
	}
}