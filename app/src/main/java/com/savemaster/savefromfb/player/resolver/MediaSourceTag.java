package com.savemaster.savefromfb.player.resolver;

import savemaster.save.master.pipd.stream.StreamInfo;
import savemaster.save.master.pipd.stream.VideoStream;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MediaSourceTag implements Serializable {
	
	@NonNull private final StreamInfo metadata;
	@NonNull private final List<VideoStream> sortedAvailableVideoStreams;
	private final int selectedVideoStreamIndex;
	
	public MediaSourceTag(@NonNull final StreamInfo metadata, @NonNull final List<VideoStream> sortedAvailableVideoStreams, final int selectedVideoStreamIndex) {
		
		this.metadata = metadata;
		this.sortedAvailableVideoStreams = sortedAvailableVideoStreams;
		this.selectedVideoStreamIndex = selectedVideoStreamIndex;
	}
	
	public MediaSourceTag(@NonNull final StreamInfo metadata) {
		this(metadata, Collections.emptyList(), -1);
	}
	
	@NonNull
	public StreamInfo getMetadata() {
		return metadata;
	}
	
	@NonNull
	public List<VideoStream> getSortedAvailableVideoStreams() {
		return sortedAvailableVideoStreams;
	}
	
	public int getSelectedVideoStreamIndex() {
		return selectedVideoStreamIndex;
	}
	
	@Nullable
	public VideoStream getSelectedVideoStream() {
		return selectedVideoStreamIndex < 0 || selectedVideoStreamIndex >= sortedAvailableVideoStreams.size() ? null : sortedAvailableVideoStreams.get(selectedVideoStreamIndex);
	}
}
