package com.oracle.instance.log.extrator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class InstanceLogExtractor {
	static StringBuilder builder = new StringBuilder();

	public static void main(String args[]) {

		final File extractedFolder = new File(args[0]);
		String instanceId = args[1];
		boolean found = false;
		if (extractedFolder.exists()) {
			for (final File fileEntry : extractedFolder.listFiles()) {
				if (fileEntry.isDirectory()) {
					if (processFolder(fileEntry.getAbsoluteFile(), instanceId)) {
						System.out
								.println("Logs for instance " + instanceId + " found in managed server " + fileEntry.getName());
						System.out.println("Logs for instance " + instanceId + " have been extracted to " + fileEntry.getAbsoluteFile() + File.separator + instanceId + ".log file.");
						found = true;
					}
				}
			}
		}
		
		if(!found){
			System.out.println("Logs for Instance " + instanceId + " not found.");
		}

	}

	public static boolean processFolder(File folder, String instanceId) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean flag = false;
				if (name.endsWith("-diagnostic.log")) {
					flag = true;
				}
				return flag;
			}
		};
		File diagnosticsFile = folder.listFiles(filter)[0];

		String outputFile = diagnosticsFile.getParent() + File.separator + instanceId + ".log";

		// read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(diagnosticsFile.getAbsolutePath()))) {

			stream.forEach(line -> {
				InstanceLogExtractor: extract(line, instanceId);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (builder.length() > 0) {

			try {
				Files.write(Paths.get(outputFile), builder.toString().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}

			builder.setLength(0);
			return true;
		}

		return false;
	}

	public static void extract(String line, String instanceId) {
		String searchTerm = "oracle.soa.tracking.FlowId: " + instanceId;
		if (line.contains(searchTerm)) {
			builder.append(line + "\n\n");
		}
	}
}
