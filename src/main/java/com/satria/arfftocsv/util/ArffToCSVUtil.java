package com.satria.arfftocsv.util;

import com.satria.arfftocsv.Collapser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonathan on 10/08/16.
 */
public class ArffToCSVUtil {

	/**
	 * Converts a list of binary values to their labeled string representation whose labels are given by the Collapser headers
	 * e.g. the header values {"Happy", "Angry", "Sad", "Tired"} and binary representation {"0", "1", "1", "0"} will return {"Angry", "Sad"}
	 * @param c
	 * @param binaryValues
	 * @return
	 */
	public static List<String> invertTransform(Collapser c, List<String> binaryValues){
		List<String> values = new ArrayList<>();

		for (int i = 0; i<binaryValues.size(); i++){
			String val = binaryValues.get(i);
			boolean value = val.equals("1") || val.equals("true");
			if (value)
				values.add(c.headers.get(i));
		}
		return values;
	}

	public static Map<String, Collapser> parseCollapserArg(List<String> collapseArgs){
		Map<String, Collapser> map = new HashMap<>();
		collapseArgs.stream().forEach(arg -> {
			String[] args = arg.split(":");
			Collapser c = new Collapser(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Boolean.parseBoolean(args[3]));
			map.put(c.name, c);
		});

		return map;
	}
}
