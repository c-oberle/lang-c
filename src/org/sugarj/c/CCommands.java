package org.sugarj.c;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sugarj.CLanguage;
import org.sugarj.common.CommandExecution;
import org.sugarj.common.CommandExecution.ExecutionError;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;

public class CCommands {
	private static final String GCC = "gcc";
	private static final String NO_LINKING_FLAG = "-c";
	private static final String C99_FLAG = "-std=c99";
	private static final String OUT_FLAG = "-o";
	private static final String INCLUDE_FLAG = "-I";
	private static final String WALL_FLAG = "-Wall";
	private static final String VERBOSE_FLAG = "-v";

	private static CLanguage lang = CLanguage.getInstance();

	public static List<Path> gcc(List<Path> outFiles, Path bin,
			List<Path> includePaths) {
		List<Path> generatedFiles = new ArrayList<Path>();

		for (Path outFile : outFiles) {
			generatedFiles.addAll(compile(outFile, bin, includePaths));
		}

		generatedFiles.addAll(link(bin));

		return generatedFiles;
	}

	public static List<Path> compile(Path outFile, Path bin,
			List<Path> includePaths) {
		System.out.println("OUT FILE: " + outFile);
		System.out.println("BIN PATH: " + bin.getAbsolutePath().toString());

		// header files are only compiled when included in a C file
		if (outFile.getAbsolutePath().endsWith(lang.getHeaderFileExtension())) {
			return Collections.emptyList();
		}

		try {
			String[] buildArgs = buildArgs(outFile, bin, includePaths);
			String[][] output = new CommandExecution(true).execute(buildArgs);

			String[] stdout = output[1];
			System.out.println("-------------------------------");
			for (int i = 0; i < stdout.length; i++) {
				System.out.println(stdout[i]);
			}
			System.out.println("-------------------------------");

			List<Path> generatedFiles = parseForObjectFiles(buildArgs);
			System.out.println("Compiled files: " + generatedFiles.size());

			return generatedFiles;
		} catch (ExecutionError e) {
			try {
				new CommandExecution(false).execute(buildArgs(outFile, bin,
						includePaths, false));
			} catch (ExecutionError _) {
			}
			return Collections.emptyList();
		}
	}

	public static List<Path> link(Path bin) {
		System.out.println("BIN PATH: " + bin.getAbsolutePath().toString());

		try {
			String[] args = getLinkingArgs(bin);
			String[][] output = new CommandExecution(true).execute(args);

			String[] stdout = output[1];
			System.out.println("-------------------------------");
			for (int i = 0; i < stdout.length; i++) {
				System.out.println(stdout[i]);
			}
			System.out.println("-------------------------------");

			List<Path> generatedFiles = parseForExecutableFiles(args);
			System.out.println("Linked executables: " + generatedFiles.size());

			return generatedFiles;
		} catch (ExecutionError e) {
			try {
				new CommandExecution(false).execute(linkArgs(bin, false));
			} catch (ExecutionError _) {
			}
			return Collections.emptyList();
		}
	}

	private static String[] buildArgs(Path outFile, Path bin,
			List<Path> includePaths, boolean verbose) {
		String binPath = bin.getAbsolutePath();
		String objFileName = outFile
				.getFile()
				.getName()
				.replace(lang.getBaseFileExtension(),
						lang.getBinaryFileExtension());
		File objFile = new File(binPath, objFileName);

		List<String> args = new LinkedList<String>();
		args.add(GCC);
		args.add(NO_LINKING_FLAG);
		args.add(C99_FLAG);

		if (verbose)
			args.add(VERBOSE_FLAG);

		args.add(WALL_FLAG);
		args.add(outFile.toString());

		for (Path p : includePaths) {
			args.add(INCLUDE_FLAG);
			args.add(p.toString());
		}

		args.add(OUT_FLAG);
		args.add(objFile.toString());

		return args.toArray(new String[args.size()]);
	}

	private static String[] buildArgs(Path outFile, Path bin,
			List<Path> includePaths) {
		return buildArgs(outFile, bin, includePaths, true);
	}

	private static String[] linkArgs(Path bin, boolean verbose) {
		String binPath = bin.getAbsolutePath();
		String execName = "ExecuteMe";
		File execFile = new File(binPath, execName);

		List<String> args = new LinkedList<String>();
		args.add(GCC);
		args.add(C99_FLAG);

		if (verbose)
			args.add(VERBOSE_FLAG);

		args.add(WALL_FLAG);

		for (File f : getObjFiles(bin)) {
			args.add(f.getPath());
		}

		args.add(OUT_FLAG);
		args.add(execFile.getPath());

		return args.toArray(new String[args.size()]);
	}

	private static File[] getObjFiles(Path bin) {
		File binDir = new File(bin.getAbsolutePath());

		// get all object files from bin directory
		FilenameFilter objFileFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return dir.isFile() && name.endsWith(".o");
			}

		};

		File[] binFiles = binDir.listFiles(objFileFilter);
		return binFiles;
	}

	private static String[] getLinkingArgs(Path bin) {
		return linkArgs(bin, true);
	}

	private static List<Path> parseForObjectFiles(String[] input) {
		List<Path> paths = new LinkedList<Path>();
		for (String s : input) {
			if (s.endsWith(".o")) {
				paths.add(new AbsolutePath(s));
			}
		}
		return paths;
	}

	private static List<Path> parseForExecutableFiles(String[] input) {
		List<Path> paths = new LinkedList<Path>();
		for (String s : input) {
			File f = new File(s);
			if (f.isFile() && !f.getName().contains(".")) {
				paths.add(new AbsolutePath(s));
			}
		}
		return paths;
	}

}
