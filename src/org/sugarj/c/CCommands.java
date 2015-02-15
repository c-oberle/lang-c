package org.sugarj.c;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sugarj.CLanguage;
import org.sugarj.common.CommandExecution;
import org.sugarj.common.CommandExecution.ExecutionError;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CCommands {
	private static final String GCC = "gcc";
	private static final String NO_LINKING_FLAG = "-c";
	private static final String C99_FLAG = "-std=c99";
	private static final String OUT_FLAG = "-o";
	private static final String INCLUDE_FLAG = "-I";
	private static final String WALL_FLAG = "-Wall";
	private static final String VERBOSE_FLAG = "-v";

	private static final String DEP_FILE_EXT = ".imports";

	private static CLanguage lang = CLanguage.getInstance();

	public static List<Path> gcc(Path outFile, Path bin,
			List<Path> includePaths, boolean link) {
		List<Path> generatedFiles = new ArrayList<Path>();

		Path compiledFile = compile(outFile, bin, includePaths);
		if (compiledFile != null)
			generatedFiles.add(compiledFile);

		if (link) {
			Path linkedFile = link(outFile, bin, includePaths);
			if (linkedFile != null)
				generatedFiles.add(linkedFile);
		}

		return generatedFiles;
	}

	public static List<Path> createDependencyFile(Path outFile,
			List<String> imports) throws IOException {
		List<Path> generatedFiles = new ArrayList<Path>();

		String absPath = outFile.getAbsolutePath();
		String name = absPath.substring(0, absPath.lastIndexOf('.'));
		String withExtension = name + DEP_FILE_EXT;
		Path filePath = new AbsolutePath(withExtension);
		File file = filePath.getFile();

		if (!file.exists()) {
			file.createNewFile();
			generatedFiles.add(filePath);
			System.out.println("Created dependency file: " + filePath);
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for (String i : imports) {
			bw.write(i);
			bw.write("\n");
		}
		bw.close();

		return generatedFiles;
	}

	private static Path compile(Path outFile, Path bin, List<Path> includePaths) {

		// don't compile header files
		if (outFile.getAbsolutePath().endsWith(lang.getHeaderFileExtension())) {
			return null;
		}

		try {
			String[] buildArgs = buildArgs(outFile, bin, includePaths);
			new CommandExecution(true).execute(buildArgs);

			Path generatedFile = parseForObjectFile(buildArgs);
			System.out.println("Compiled file: " + generatedFile);

			return generatedFile;
		} catch (ExecutionError e) {
			try {
				new CommandExecution(false).execute(buildArgs(outFile, bin,
						includePaths, false));
			} catch (ExecutionError _) {
			}
			return null;
		}
	}

	private static Path link(Path outFile, Path bin, List<Path> includePaths) {

		System.out.println("----------Include paths:----------");

		for (Path p : includePaths) {
			System.out.println(p);
		}

		System.out.println("----------------------------------");

		try {
			String[] args = getLinkingArgs(outFile, bin, includePaths);
			String[][] output = new CommandExecution(true).execute(args);

			String[] stdout = output[1];
			System.out.println("-------------------------------");
			for (int i = 0; i < stdout.length; i++) {
				System.out.println(stdout[i]);
			}
			System.out.println("-------------------------------");

			Path generatedFile = parseForExecutableFile(args);
			System.out.println("Linked executable: " + generatedFile);

			return generatedFile;
		} catch (ExecutionError e) {
			try {
				new CommandExecution(false).execute(linkArgs(outFile, bin,
						includePaths, false));
			} catch (ExecutionError _) {
			}
			return null;
		}
	}

	private static List<Path> getImports(Path outFile, List<Path> includePaths)
			throws IOException {
		List<Path> imports = new ArrayList<Path>();

		String absPath = outFile.getAbsolutePath();
		RelativePath filePath = new RelativePath(absPath);
		filePath = FileCommands.replaceExtension(filePath, DEP_FILE_EXT);
		File file = filePath.getFile();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));

			String line = reader.readLine();
			while (line != null) {
				imports.add(getAbsolutePathForImport(line, includePaths));
				line = reader.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return imports;
	}

	private static AbsolutePath getAbsolutePathForImport(String moduleName,
			List<Path> includePaths) {
		AbsolutePath path = null;
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(DEP_FILE_EXT);
			}
		};
		for (Path p : includePaths) {
			if (p.getAbsolutePath().endsWith(".jar"))
				continue;
			RelativePath[] matchingFiles = FileCommands.listFiles(p, filter);
			for (int i = 0; i < matchingFiles.length; i++) {
				String absPath = matchingFiles[i].getAbsolutePath();
				if (absPath.endsWith(moduleName + DEP_FILE_EXT)) {
					path = new AbsolutePath(absPath);
					break;
				}
			}
		}
		return path;
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

	private static String[] linkArgs(Path outFile, Path bin,
			List<Path> includePaths, boolean verbose) {
		String binPath = bin.getAbsolutePath();
		String execName = outFile.getFile().getName().split("\\.")[0];
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
				return name.endsWith(".o");
			}

		};

		File[] binFiles = binDir.listFiles(objFileFilter);
		return binFiles;
	}

	private static String[] getLinkingArgs(Path outFile, Path bin,
			List<Path> includePaths) {
		return linkArgs(outFile, bin, includePaths, true);
	}

	private static Path parseForObjectFile(String[] input) {
		for (String s : input) {
			if (s.endsWith(".o")) {
				return new AbsolutePath(s);
			}
		}
		return null;
	}

	private static Path parseForExecutableFile(String[] input) {
		for (String s : input) {
			File f = new File(s);
			if (f.isFile() && !f.getName().contains(".")) {
				return new AbsolutePath(s);
			}
		}
		return null;
	}

}
