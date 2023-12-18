package com.savemaster.savefromfb.models.response.explore;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ExampleQuery2{

	@SerializedName("runs")
	private List<RunsItem> runs;

	public List<RunsItem> getRuns(){
		return runs;
	}

	@Override
 	public String toString(){
		return 
			"ExampleQuery2{" + 
			"runs = '" + runs + '\'' + 
			"}";
		}
}