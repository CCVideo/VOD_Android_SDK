package com.bokecc.sdk.mobile.demo.upload.category;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainCategory extends Category {
	List<Category> list = new ArrayList<Category>();
	
	public MainCategory(JSONObject jsonObject) throws JSONException {
		this.id = jsonObject.getString("id");
		this.name = jsonObject.getString("name");
		
		JSONArray subCategoryArray = jsonObject.getJSONArray("sub-category");
		for (int i=0; i<subCategoryArray.length(); i++) {
			add(new SubCategory( subCategoryArray.getJSONObject(i)));
		}
	}

	@Override
	public void add(Category category) {
		list.add(category);
	}

	@Override
	public void del(Category category) {
		list.remove(category);
		
	}

	@Override
	public Category get(int i) {
		return list.get(i);
	}

	@Override
	public int getCount() {
		if (list == null) {
			return 0;
		}
		return list.size();
	}

}
