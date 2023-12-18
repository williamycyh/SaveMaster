package com.savemaster.savefromfb.models.response.explore;

import com.google.gson.annotations.SerializedName;

public class UntoggledIcon{

	@SerializedName("iconType")
	private String iconType;

	public String getIconType(){
		return iconType;
	}

	@Override
 	public String toString(){
		return 
			"UntoggledIcon{" + 
			"iconType = '" + iconType + '\'' + 
			"}";
		}
}