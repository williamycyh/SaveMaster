package com.savemaster.savefromfb.models.response.explore;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class PlaceholderHeader{

	@SerializedName("runs")
	private List<RunsItem> runs;

	public List<RunsItem> getRuns(){
		return runs;
	}

	@Override
 	public String toString(){
		return 
			"PlaceholderHeader{" + 
			"runs = '" + runs + '\'' + 
			"}";
		}
}