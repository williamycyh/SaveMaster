package com.savemaster.savefromfb.models.response.explore;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class TwoColumnBrowseResultsRenderer{

	@SerializedName("tabs")
	private List<TabsItem> tabs;

	public List<TabsItem> getTabs(){
		return tabs;
	}

	@Override
 	public String toString(){
		return 
			"TwoColumnBrowseResultsRenderer{" + 
			"tabs = '" + tabs + '\'' + 
			"}";
		}
}