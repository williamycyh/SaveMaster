package com.savemaster.savefromfb.player.playqueue.events;

public class InitEvent implements PlayQueueEvent {
	
	@Override
	public PlayQueueEventType type() {
		return PlayQueueEventType.INIT;
	}
}
