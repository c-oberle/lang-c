package org.sugarj.c;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
			sb.append("Import:");
			sb.append(i.substring(i.indexOf("\"") + 1, i.lastIndexOf("\"")));
			sb.append("\n");
		}
		for (String d : deps) {
			sb.append("Dep:");
			sb.append(d);
			sb.append("\n");
		}
		sb.append("\n");

		FileCommands.writeToFile(filePath, sb.toString());

		return generatedFiles;
	}

	private static Path compile(Path outFile, Path bin, List<Path> includePaths) {
		// don't compile header files
		if (outFile.getAbsolutePath().endsWith(lang.getHeaderFileExtension()))
			return null;

		try {
			String[] buildArgs = buildArgs(outFile, bin, includePaths);
			new CommandExecution(true).execute(buildArgs);
			Path generatedFile = parseForObjectFile(buildArgs);
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

	// private static Set<AbsolutePath> getDepsRecursively(Path outFile,
	// List<Path> includePaths) {
	// Set<AbsolutePath> recDeps = new HashSet<AbsolutePath>();
	// Set<AbsolutePath> deps = getDeps(outFile, includePaths);
	// recDeps.addAll(deps);
	//
	// for (AbsolutePath dep : deps) {
	// if (!recDeps.contains(dep))
	// recDeps.addAll(getDepsRecursively(dep, includePaths));
	// }
	//
	// return deps;
	// }
	//
	// private static Set<AbsolutePath> getDeps(Path outFile,
	// List<Path> includePaths) {
	// Set<AbsolutePath> deps = new HashSet<AbsolutePath>();
	// Set<AbsolutePath> imports = getImportsRecursively(outFile, includePaths);
	// deps.addAll(imports);
	//
	// // remove non-header files
	// for (AbsolutePath dep : deps) {
	// if (FileCommands.dropExtension(dep.getAbsolutePath()).endsWith(
	// lang.getHeaderSuffix())) {
	// deps.remove(dep);
	// }
	// }
	//
	// deps.addAll(getDepsRecursively(outFile, includePaths));
	//
	// return deps;
	// }

	private static Set<AbsolutePath> getDependenciesRec(Path outFile,
			List<Path> includePaths) {
		Set<AbsolutePath> directDeps = getDependencies(outFile, includePaths);
		Set<AbsolutePath> recDeps = directDeps;

		for (AbsolutePath dep : directDeps) {
			recDeps.addAll(getDependenciesRec(dep, includePaths));
		}

		return recDeps;
	}

	private static Set<AbsolutePath> getDependencies(Path outFile,
			List<Path> includePaths) {
		Set<AbsolutePath> deps = new HashSet<AbsolutePath>();

		AbsolutePath filePath = new AbsolutePath(outFile.getAbsolutePath());
		filePath = FileCommands.replaceExtension(filePath, DEP_FILE_EXT);

		String content = null;
		try {
			content = FileCommands.readFileAsString(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (content != null) {
			String lines[] = content.split("\\r?\\n");

			for (String line : lines) {
				if (line.startsWith("D"))
					try {
						String path = line.substring(line.indexOf(":") + 1);
						deps.add(getAbsolutePathForImport(path, includePaths));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
			}
		}

		Set<AbsolutePath> refs = new HashSet<AbsolutePath>();
		refs.addAll(getImports(outFile, includePaths));
		refs.addAll(deps);

		for (AbsolutePath ref : refs) {
			deps.addAll(getDependencies(ref, includePaths));
		}

		return deps;
	}

	// private static Set<AbsolutePath> getImportsRecursively(Path outFile,
	// List<Path> includePaths) {
	// Set<AbsolutePath> recImports = new HashSet<AbsolutePath>();
	// Set<AbsolutePath> directImports = getImports(outFile, includePaths);
	// recImports.addAll(directImports);
	//
	// for (AbsolutePath file : directImports) {
	// if (!recImports.contains(file))
	// recImports.addAll(getImportsRecursively(file, includePaths));
	// }
	//
	// return recImports;
	// }

	private static Set<AbsolutePath> getImports(Path outFile,
			List<Path> includePaths) {
		Set<AbsolutePath> imports = new HashSet<AbsolutePath>();
		AbsolutePath filePath = new AbsolutePath(outFile.getAbsolutePath());
		filePath = FileCommands.replaceExtension(filePath, DEP_FILE_EXT);

		String content = null;
		try {
			content = FileCommands.readFileAsString(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (content != null) {
			String lines[] = content.split("\\r?\\n");

			for (String line : lines) {
				if (line.startsWith("I"))
					try {
						String path = line.substring(line.indexOf(":") + 1);
						imports.add(getAbsolutePathForImport(path, includePaths));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
			}
		}

		return imports;
	}

	private static AbsolutePath getAbsolutePathForImport(
			final String moduleName, List<Path> includePaths)
			throws FileNotFoundException {
		System.out.println("GET ABS PATH FOR : " + moduleName);
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

	private static String[] buildArgs(Path outFile, Path bin,
			List<Path> includePaths) {
		return buildArgs(outFile, bin, includePaths, true);
	}

	private static String[] buildArgs(Path outFile, Path bin,
			List<Path> includePaths, boolean verbose) {
		AbsolutePath absOutFile = new AbsolutePath(outFile.getAbsolutePath());
		absOutFile = FileCommands.replaceExtension(absOutFile,
				lang.getBinaryFileExtension());

		List<String> args = new LinkedList<String>();
		args.addAll(getStandardArgsForGCC(verbose));
		args.add(NO_LINKING_FLAG);
		args.add(outFile.toString());

		for (Path p : includePaths) {
			args.add(INCLUDE_FLAG);
			args.add(p.toString());
		}

		args.add(OUT_FLAG);
		args.add(absOutFile.getAbsolutePath());

		return args.toArray(new String[args.size()]);
	}

	private static String[] getLinkingArgs(Path outFile, Path bin,
			List<Path> includePaths) {
		return getLinkingArgs(outFile, bin, includePaths, true);
	}

	private static String[] getLinkingArgs(Path outFile, Path bin,
			List<Path> includePaths, boolean verbose) {
		String execFile = FileCommands.dropExtension(outFile.getAbsolutePath());
		Set<AbsolutePath> deps = getDependencies(outFile, includePaths);

		List<String> args = new LinkedList<String>();
		args.addAll(getStandardArgsForGCC(verbose));
		args.add(outFile.getAbsolutePath());

		for (AbsolutePath dep : deps) {
			Path objFile = FileCommands.replaceExtension(
					new AbsolutePath(dep.getAbsolutePath()),
					lang.getBinaryFileExtension());
			args.add(objFile.getAbsolutePath());
		}

		args.add(OUT_FLAG);
		args.add(execFile);

		return args.toArray(new String[args.size()]);
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

	// private static String dropHeaderSuffix(String fileName) {
	// String suffix = lang.getHeaderSuffix();
	// if (fileName.endsWith(suffix))
	// return fileName.substring(0, fileName.length() - suffix.length());
	//
	// return fileName;
	// }

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
