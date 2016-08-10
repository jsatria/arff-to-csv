package com.satria;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Seq;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		parser.addArgument("-c").help("collapse columns into pipe delimited column entered as comma separated tuples of column_name:[start_index]:[end_index]").setDefault("");
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

			Boolean header = Boolean.valueOf((String) params.get("header"));

			List<String> collapsers = Arrays.asList(((String) params.get("c")).split(" "));

			//collect header info
			if (header) {
				headerItems = bis.lines()
						.filter(s -> s.startsWith("@attribute"))
						.map(s -> (String) Array.get(s.split(" "), 1))
						.collect(Collectors.toList());

				collapsers.stream().forEach(c -> {
					String[] collapseParams = c.split(":");
					int start = Integer.valueOf(collapseParams[1]);
					int end = Integer.valueOf(collapseParams[2]) +1 ;

					headerItems.subList(start, end).clear();
					headerItems.add(start, collapseParams[0]);
				});
				bos.write(StringUtils.join(headerItems, ","));
				bos.flush();
				bis = new BufferedReader(new FileReader(inputFile));//reset
			}

			lines = Seq.ofType(bis.lines(), String.class).skipUntil(s -> s.startsWith("@data")).skip(1).stream()
					.collect(Collectors.toList());

			lines.stream()
					.forEach(s ->
					{
						List<String> values = Arrays.stream(s.split(",")).collect(Collectors.toList());
						collapsers.stream().forEach(c -> {
							String[] collapseParams = c.split(":");
							int start = Integer.valueOf(collapseParams[1]);
							int end = Integer.valueOf(collapseParams[2]) + 1;

							String newString = StringUtils.join(values.subList(start, end), "|");
							values.subList(start, end).clear();
							values.add(start, newString);
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

		} catch (FileNotFoundException e){
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}


	}
}
