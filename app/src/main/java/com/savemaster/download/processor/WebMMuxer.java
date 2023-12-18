package com.savemaster.download.processor;

import com.savemaster.savefromfb.streams.WebMReader;
import com.savemaster.savefromfb.streams.WebMWriter;
import com.savemaster.savefromfb.streams.io.SharpStream;

import java.io.IOException;

class WebMMuxer extends PostProcessing {
	
	WebMMuxer() {
		super(true, true, ALGORITHM_WEBM_MUXER);
	}
	
	@Override
	int process(SharpStream out, SharpStream... sources) throws IOException {
		WebMWriter muxer = new WebMWriter(sources);
		muxer.parseSources();
		
		// youtube uses a webm with a fake video track that acts as a "cover image"
		int[] indexes = new int[sources.length];
		
		for (int i = 0; i < sources.length; i++) {
			WebMReader.WebMTrack[] tracks = muxer.getTracksFromSource(i);
			for (int j = 0; j < tracks.length; j++) {
				if (tracks[j].kind == WebMReader.TrackKind.Audio) {
					indexes[i] = j;
					i = sources.length;
					break;
				}
			}
		}
		
		muxer.selectTracks(indexes);
		muxer.build(out);
		
		return OK_RESULT;
	}
	
}
