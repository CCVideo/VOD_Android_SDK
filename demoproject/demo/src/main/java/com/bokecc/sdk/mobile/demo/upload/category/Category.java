package com.bokecc.sdk.mobile.demo.upload.category;

public abstract class Category {
	String name;
	String id;
	public abstract void add(Category category);
	public abstract void del(Category category);
	public abstract Category get(int i);
	public abstract int getCount();
	public String getName() {
		return name;
	}
	public String getId() {
		return id;
	}
}
