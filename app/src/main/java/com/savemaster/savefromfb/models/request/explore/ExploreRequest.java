package com.savemaster.savefromfb.models.request.explore;

import com.google.gson.annotations.SerializedName;

public class ExploreRequest {

	@SerializedName("browseId")
	public String browseId;

	@SerializedName("context")
	public Context context;

	@SerializedName("params")
	public String params;
}