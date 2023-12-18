package com.savemaster.savefromfb.models.response.explore;

import com.google.gson.annotations.SerializedName;

public class UntoggledAccessibility{

	@SerializedName("accessibilityData")
	private AccessibilityData accessibilityData;

	public AccessibilityData getAccessibilityData(){
		return accessibilityData;
	}

	@Override
 	public String toString(){
		return 
			"UntoggledAccessibility{" + 
			"accessibilityData = '" + accessibilityData + '\'' + 
			"}";
		}
}