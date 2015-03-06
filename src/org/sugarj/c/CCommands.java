package org.sugarj.c;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	private static final String DEP_FILE_EXT = "imports";
	private static final String IMPORT_PREFIX = "Import:";
	private static final String DEP_PREFIX = "Dep:";

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

	public static List<Path> writeDependencyFile(Path outFile,
			List<String> imports, List<String> deps) throws IOException {
		List<Path> generatedFiles = new ArrayList<Path>();
		String absPath = outFile.getAbsolutePath();
		Path filePath = FileCommands.replaceExtension(
				new AbsolutePath(absPath), DEP_FILE_EXT);

		FileCommands.createFile(filePath);
		generatedFiles.add(filePath);

		StringBuilder sb = new StringBuilder();
		for (String i : imports) {
			sb.append(IMPORT_PREFIX);
			sb.append(i.substring(i.indexOf("\"") + 1, i.lastIndexOf("\"")));
			sb.append("\n");
		}
		for (String d : deps) {
			sb.append(DEP_PREFIX);
			sb.append(d);
			sb.append("\n");
		}
		FileCommands.writeToFile(filePath, sb.toString());

		return generatedFiles;
	}

	private static Path compile(Path outFile, Path bin, List<Path> includePaths) {
		// don't compile header files
		// if (FileCommands.dropExtension(outFile.getAbsolutePath()).endsWith(
		// lang.getHeaderSuffix()))
		// return null;

		// try {
		String[] args = getCompileArgs(outFile, bin, includePaths);
		new CommandExecution(true).execute(args);
		Path generatedFile = parseForObjectFile(args);
		return generatedFile;
		// } catch (ExecutionError e) {
		// try {
		// new CommandExecution(false).execute(getBuildArgs(outFile, bin,
		// includePaths, false));
		// } catch (ExecutionError _) {
		// }
		// return null;
		// }
	}

	private static Path link(Path outFile, Path bin, List<Path> includePaths) {
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
			return generatedFile;
		} catch (ExecutionError e) {
			try {
				new CommandExecution(false).execute(getLinkingArgs(outFile,
						bin, includePaths, false));
			} catch (ExecutionError _) {
			}
			return null;
		}
	}

	private static Set<AbsolutePath> getDependencies(Path outFile,
			List<Path> includePaths) {
		HashMap<String, Set<AbsolutePath>> refMap = readDependencyFile(outFile,
				includePaths);
		Set<AbsolutePath> deps = new HashSet<AbsolutePath>();
		deps.addAll(refMap.get(DEP_PREFIX));

		Set<AbsolutePath> refs = new HashSet<AbsolutePath>();
		refs.addAll(refMap.get(IMPORT_PREFIX));
		refs.addAll(deps);

		for (AbsolutePath ref : refs) {
			deps.addAll(getDependencies(ref, includePaths));
		}

		return deps;
	}

	private static HashMap<String, Set<AbsolutePath>> readDependencyFile(
			Path outFile, List<Path> includePaths) {
		HashMap<String, Set<AbsolutePath>> refs = new HashMap<String, Set<AbsolutePath>>();
		Set<AbsolutePath> imports = new HashSet<AbsolutePath>();
		Set<AbsolutePath> deps = new HashSet<AbsolutePath>();
		AbsolutePath filePath = new AbsolutePath(outFile.getAbsolutePath());
		filePath = FileCommands.replaceExtension(filePath, DEP_FILE_EXT);

		String content = null;
		try {
			content = FileCommands.readFileAsString(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (content == null)
			return refs;

		String lines[] = content.split("\\r?\\n");

		for (String line : lines) {
			if (!line.isEmpty())
				try {
					String path = line.substring(line.indexOf(":") + 1);
					AbsolutePath absPath = getAbsolutePathForImport(path,
							includePaths);
					if (line.startsWith(IMPORT_PREFIX))
						imports.add(absPath);
					else if (line.startsWith(DEP_PREFIX))
						deps.add(absPath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
		}

		refs.put(IMPORT_PREFIX, imports);
		refs.put(DEP_PREFIX, deps);

		return refs;
	}

	private static AbsolutePath getAbsolutePathForImport(
			final String moduleName, List<Path> includePaths)
			throws FileNotFoundException {
		AbsolutePath path = null;
		boolean found = false;

		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getPath().endsWith(moduleName);
			}
		};
		for (Path p : includePaths) {
			if (p.getAbsolutePath().endsWith(".jar"))
				continue;
			List<RelativePath> matchingFiles = FileCommands.listFilesRecursive(
					p, filter);
			if (!matchingFiles.isEmpty()) {
				String absPath = matchingFiles.get(0).getAbsolutePath();
				path = new AbsolutePath(absPath);
				found = true;
				break;
			}
		}
		if (!found)
			throw new FileNotFoundException();
		return path;
	}

	private static String[] getCompileArgs(Path outFile, Path bin,
			List<Path> includePaths) {
		return getCompileArgs(outFile, bin, includePaths, true);
	}

	private static String[] getCompileArgs(Path outFile, Path bin,
			List<Path> includePaths, boolean verbose) {
		List<String> args = new LinkedList<String>();
		args.addAll(getStandardArgsForGCC(verbose));
		args.add(NO_LINKING_FLAG);
		args.add(outFile.toString());

		for (Path p : includePaths)
			args.add(INCLUDE_FLAG + p.toString());

		args.add(OUT_FLAG);
		args.add(getBinaryFile(outFile));

		return args.toArray(new String[args.size()]);
	}

	private static String[] getLinkingArgs(Path outFile, Path bin,
			List<Path> includePaths) {
		return getLinkingArgs(outFile, bin, includePaths, true);
	}

	private static String[] getLinkingArgs(Path outFile, Path bin,
			List<Path> includePaths, boolean verbose) {
		String execFile = FileCommands.dropExtension(outFile.getAbsolutePath());
		List<String> args = new LinkedList<String>();

		args.addAll(getStandardArgsForGCC(verbose));
		args.add(getBinaryFile(outFile));

		for (AbsolutePath dep : getDependencies(outFile, includePaths))
			args.add(getBinaryFile(dep));

		args.add(OUT_FLAG);
		args.add(execFile);

		return args.toArray(new String[args.size()]);
	}

	private static String getBinaryFile(Path outFile) {
		return FileCommands.replaceExtension(
				new AbsolutePath(outFile.getAbsolutePath()),
				lang.getBinaryFileExtension()).getAbsolutePath();
	}

	private static List<String> getStandardArgsForGCC(boolean verbose) {
		List<String> args = new LinkedList<String>();
		args.add(GCC);
		args.add(C99_FLAG);
		if (verbose)
			args.add(VERBOSE_FLAG);
		args.add(WALL_FLAG);
		return args;
	}

	private static Path parseForObjectFile(String[] input) {
		for (String s : input) {
			if (s.endsWith("." + lang.getBinaryFileExtension())) {
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
