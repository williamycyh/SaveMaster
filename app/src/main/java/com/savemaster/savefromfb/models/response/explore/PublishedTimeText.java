package com.savemaster.savefromfb.models.response.explore;

import com.google.gson.annotations.SerializedName;

public class PublishedTimeText{

	@SerializedName("simpleText")
	private String simpleText;

	public String getSimpleText(){
		return simpleText;
	}

	@Override
 	public String toString(){
		return 
			"PublishedTimeText{" + 
			"simpleText = '" + simpleText + '\'' + 
			"}";
		}
}