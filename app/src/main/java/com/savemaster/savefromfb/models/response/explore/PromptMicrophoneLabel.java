package com.savemaster.savefromfb.models.response.explore;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class PromptMicrophoneLabel{

	@SerializedName("runs")
	private List<RunsItem> runs;

	public List<RunsItem> getRuns(){
		return runs;
	}

	@Override
 	public String toString(){
		return 
			"PromptMicrophoneLabel{" + 
			"runs = '" + runs + '\'' + 
			"}";
		}
}