package com.savemaster.savefromfb.db.playlist.model;

import android.text.TextUtils;

import savemaster.save.master.pipd.playlist.PlaylistInfo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.savemaster.savefromfb.db.LocalItem;
import com.savemaster.savefromfb.db.playlist.PlaylistLocalItem;

import static com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity.REMOTE_PLAYLIST_NAME;
import static com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity.REMOTE_PLAYLIST_SERVICE_ID;
import static com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity.REMOTE_PLAYLIST_TABLE;
import static com.savemaster.savefromfb.db.playlist.model.PlaylistRemoteEntity.REMOTE_PLAYLIST_URL;

@Entity(tableName = REMOTE_PLAYLIST_TABLE,
		indices = {
				@Index(value = {REMOTE_PLAYLIST_NAME}),
				@Index(value = {REMOTE_PLAYLIST_SERVICE_ID, REMOTE_PLAYLIST_URL}, unique = true)
		})
public class PlaylistRemoteEntity implements PlaylistLocalItem {
	final public static String REMOTE_PLAYLIST_TABLE = "remote_playlists";
	final public static String REMOTE_PLAYLIST_ID = "uid";
	final public static String REMOTE_PLAYLIST_SERVICE_ID = "service_id";
	final public static String REMOTE_PLAYLIST_NAME = "name";
	final public static String REMOTE_PLAYLIST_URL = "url";
	final public static String REMOTE_PLAYLIST_THUMBNAIL_URL = "thumbnail_url";
	final public static String REMOTE_PLAYLIST_UPLOADER_NAME = "uploader";
	final public static String REMOTE_PLAYLIST_STREAM_COUNT = "stream_count";
	
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = REMOTE_PLAYLIST_ID)
	private long uid = 0;
	
	@ColumnInfo(name = REMOTE_PLAYLIST_SERVICE_ID)
	private int serviceId;
	
	@ColumnInfo(name = REMOTE_PLAYLIST_NAME)
	private String name;
	
	@ColumnInfo(name = REMOTE_PLAYLIST_URL)
	private String url;
	
	@ColumnInfo(name = REMOTE_PLAYLIST_THUMBNAIL_URL)
	private String thumbnailUrl;
	
	@ColumnInfo(name = REMOTE_PLAYLIST_UPLOADER_NAME)
	private String uploader;
	
	@ColumnInfo(name = REMOTE_PLAYLIST_STREAM_COUNT)
	private Long streamCount;
	
	public PlaylistRemoteEntity(int serviceId, String name, String url, String thumbnailUrl,
								String uploader, Long streamCount) {
		this.serviceId = serviceId;
		this.name = name;
		this.url = url;
		this.thumbnailUrl = thumbnailUrl;
		this.uploader = uploader;
		this.streamCount = streamCount;
	}
	
	@Ignore
	public PlaylistRemoteEntity(final PlaylistInfo info) {
		this(info.getServiceId(), info.getName(), info.getUrl(),
			 info.getThumbnailUrl() == null ? info.getUploaderAvatarUrl() : info.getThumbnailUrl(),
			 info.getUploaderName(), info.getStreamCount());
	}
	
	@Ignore
	public boolean isIdenticalTo(final PlaylistInfo info) {
		/*
		 * Returns boolean comparing the online playlist and the local copy.
		 * (False if info changed such as playlist name or track count)
		 */
		return getServiceId() == info.getServiceId()
				&& getStreamCount() == info.getStreamCount()
				&& TextUtils.equals(getName(), info.getName())
				&& TextUtils.equals(getUrl(), info.getUrl())
				&& TextUtils.equals(getThumbnailUrl(), info.getThumbnailUrl())
				&& TextUtils.equals(getUploader(), info.getUploaderName());
	}
	
	public long getUid() {
		return uid;
	}
	
	public void setUid(long uid) {
		this.uid = uid;
	}
	
	public int getServiceId() {
		return serviceId;
	}
	
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUploader() {
		return uploader;
	}
	
	public void setUploader(String uploader) {
		this.uploader = uploader;
	}
	
	public Long getStreamCount() {
		return streamCount;
	}
	
	public void setStreamCount(Long streamCount) {
		this.streamCount = streamCount;
	}
	
	@Override
	public LocalItem.LocalItemType getLocalItemType() {
		return LocalItem.LocalItemType.PLAYLIST_REMOTE_ITEM;
	}
	
	@Override
	public String getOrderingName() {
		return name;
	}
}
