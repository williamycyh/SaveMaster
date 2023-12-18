package com.savemaster.savefromfb.models.response.explore;

import com.google.gson.annotations.SerializedName;

public class ServiceEndpoint{

	@SerializedName("commandMetadata")
	private CommandMetadata commandMetadata;

	@SerializedName("clickTrackingParams")
	private String clickTrackingParams;

	@SerializedName("signalServiceEndpoint")
	private SignalServiceEndpoint signalServiceEndpoint;

	public CommandMetadata getCommandMetadata(){
		return commandMetadata;
	}

	public String getClickTrackingParams(){
		return clickTrackingParams;
	}

	public SignalServiceEndpoint getSignalServiceEndpoint(){
		return signalServiceEndpoint;
	}

	@Override
 	public String toString(){
		return 
			"ServiceEndpoint{" + 
			"commandMetadata = '" + commandMetadata + '\'' + 
			",clickTrackingParams = '" + clickTrackingParams + '\'' + 
			",signalServiceEndpoint = '" + signalServiceEndpoint + '\'' + 
			"}";
		}
}