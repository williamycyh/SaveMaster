package com.savemaster.savefromfb.local.playlist;

import com.savemaster.savefromfb.db.AppDatabase;
import com.savemaster.savefromfb.db.playlist.dao.PlaylistRemoteDAO;
import com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity;

import savemaster.save.master.pipd.playlist.PlaylistInfo;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RemotePlaylistManager {
	
	private final PlaylistRemoteDAO playlistRemoteTable;
	
	public RemotePlaylistManager(final AppDatabase db) {
		
		playlistRemoteTable = db.playlistRemoteDAO();
	}
	
	public Flowable<List<PlaylistRemoteEntity>> getPlaylists() {
		
		return playlistRemoteTable.getAll().subscribeOn(Schedulers.io());
	}
	
	public Flowable<List<PlaylistRemoteEntity>> getPlaylist(final PlaylistInfo info) {
		
		return playlistRemoteTable.getPlaylist(info.getServiceId(), info.getUrl()).subscribeOn(Schedulers.io());
	}
	
	public Single<Integer> deletePlaylist(final long playlistId) {
		
		return Single.fromCallable(() -> playlistRemoteTable.deletePlaylist(playlistId)).subscribeOn(Schedulers.io());
	}
	
	public Single<Long> onBookmark(final PlaylistInfo playlistInfo) {
		
		return Single.fromCallable(() -> {
			
			final PlaylistRemoteEntity playlist = new PlaylistRemoteEntity(playlistInfo);
			return playlistRemoteTable.upsert(playlist);
			
		}).subscribeOn(Schedulers.io());
	}
	
	public Single<Integer> onUpdate(final long playlistId, final PlaylistInfo playlistInfo) {
		
		return Single.fromCallable(() -> {
			
			PlaylistRemoteEntity playlist = new PlaylistRemoteEntity(playlistInfo);
			playlist.setUid(playlistId);
			
			return playlistRemoteTable.update(playlist);
			
		}).subscribeOn(Schedulers.io());
	}
}
