package com.satria.arfftocsv;

import java.util.List;

/**
 * Created by jonathan on 10/08/16.
 */
public class Collapser {
	public int start, end;
	public String name;
	public List<String> headers;
	public boolean invertTransform;

	public Collapser(String name, int start, int end, boolean invertTransform){
		this.start = start;
		this.end = end;
		this.name = name;
		this.invertTransform = invertTransform;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
}
