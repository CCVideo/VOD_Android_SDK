package com.bokecc.sdk.mobile.demo.upload.category;

import org.json.JSONException;
import org.json.JSONObject;

public class SubCategory extends Category {
	
	public SubCategory(JSONObject jsonObject) throws JSONException {
		this.id = jsonObject.getString("id");
		this.name = jsonObject.getString("name");
	}

	@Override
	public void add(Category category) {
	}

	@Override
	public void del(Category category) {
	}

	@Override
	public Category get(int i) {
		return null;
	}

	@Override
	public int getCount() {
		return 0;
	}

}
