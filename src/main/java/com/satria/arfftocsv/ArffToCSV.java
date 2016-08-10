package com.satria.arfftocsv;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Seq;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static com.satria.arfftocsv.util.ArffToCSVUtil.*;

/**
 * Created by jonathan on 27/07/16.
 */
public class ArffToCSV {
	static List<String> headerItems = null;
	static List<String> lines = null;

	public static void main(String[] args){
		Map<String, Object> params = new HashMap();
		ArgumentParser parser = ArgumentParsers.newArgumentParser("ArffToCSV", true).defaultHelp(true).description("Convert ARFF to CSV");
		parser.addArgument("-i").help("input arff filepath").required(true);
		parser.addArgument("-o").help("output csv filepath").setDefault("." + File.separator + "convert.csv");

		/** WIP: Collapsing columns into PSV **/
		parser.addArgument("-c").help("collapse columns into pipe delimited column entered as comma separated tuples of [column_name]:[start_index]:[end_index]:[invertTransform]").setDefault("");
		parser.addArgument("-header").help("schema file").setDefault(false);

		try {
			parser.parseArgs(args, params);
		} catch (ArgumentParserException e){
			e.printStackTrace();
			System.exit(1);
		}

		// read file
		File inputFile = new File((String) params.get("i"));
		File outputFile = new File((String) params.get("o"));

		try {
			if (!outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			if (!outputFile.exists())
				outputFile.createNewFile();

			BufferedReader bis = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bos = new BufferedWriter(new FileWriter(outputFile));

			Map<String, Collapser> collapsers = parseCollapserArg(Arrays.asList(((String) params.get("c")).split(" ")));

			Boolean header = Boolean.valueOf((String) params.get("header"));

			//collect header info
			if (header) {
				headerItems = bis.lines()
						.filter(s -> s.startsWith("@attribute"))
						.map(s -> (String) Array.get(s.split(" "), 1))
						.collect(Collectors.toList());

				collapsers.entrySet().stream().forEach(x -> {
					Collapser c =  x.getValue();
					List<String> items = headerItems.subList(c.start, c.end);
					c.setHeaders(new ArrayList<>(headerItems.subList(c.start, c.end)));
					items.clear();
					headerItems.add(c.start, c.name);
				});

				bos.write(StringUtils.join(headerItems, ","));
				bos.flush();
				bis.close();
				bis = new BufferedReader(new FileReader(inputFile));//reset
			}

			lines = Seq.ofType(bis.lines(), String.class).skipUntil(s -> s.startsWith("@data")).skip(1).stream()
					.collect(Collectors.toList());

			lines.stream()
					.forEach(s ->
					{
						List<String> values = Arrays.stream(s.split(",")).collect(Collectors.toList());
						collapsers.entrySet().stream().forEach(x -> {
							Collapser c = x.getValue();
							List<String> subValues = values.subList(c.start, c.end);
							String newValueString = "";

							if (c.invertTransform){
								List<String> invertedValuesString = invertTransform(c, subValues);
								newValueString = StringUtils.join(invertedValuesString, "|");
							}

							else {
								newValueString = StringUtils.join(values.subList(c.start, c.end), "|");
							}

							values.subList(c.start, c.end).clear();
							values.add(c.start, newValueString);
						});
						s = StringUtils.join(values, ",");

						try {
							bos.newLine();
							bos.write(s);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});

			bos.flush();

			bos.close();
			bis.close();

		} catch (FileNotFoundException e){
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
