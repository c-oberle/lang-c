package org.sugarj;

import static org.sugarj.common.ATermCommands.getApplicationSubterm;
import static org.sugarj.common.ATermCommands.isApplication;
import static org.sugarj.util.TermFinder.mayFind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.c.CCommands;
import org.sugarj.common.ATermCommands;
import org.sugarj.baselang.IORelay;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CProcessor extends AbstractBaseProcessor {

	private static final long serialVersionUID = 2057395343737713876L;

	private CLanguage lang = CLanguage.getInstance();
	private List<String> pragmas = new LinkedList<String>();
	private List<String> imports = new LinkedList<String>();
	private List<String> deps = new LinkedList<String>();
	private List<String> body = new LinkedList<String>();

	private Path outFile;
	private String relFileName;
	private boolean isMain;
	private boolean isHeader;

	private IStrategoTerm ppTable;

	@Override
	public String getGeneratedSource() {
		if (body.isEmpty())
			return "";

		StringBuilder sourceBuilder = new StringBuilder();
		sourceBuilder.append(StringCommands.printListSeparated(pragmas, "\n"))
				.append("\n");
		sourceBuilder.append(StringCommands.printListSeparated(imports, "\n"))
				.append("\n\n");
		sourceBuilder.append(StringCommands.printListSeparated(body, "\n"));
		String source = sourceBuilder.toString();

		return source;
	}

	@Override
	public Path getGeneratedSourceFile() {
		return outFile;
	}

	@Override
	public String getNamespace() {
		int i = relFileName.lastIndexOf(Environment.sep);
		String namespace = i > 0 ? relFileName.substring(0, i) : "";
		return namespace;
	}

	@Override
	public CLanguage getLanguage() {
		return lang;
	}

	@Override
	public void init(Set<RelativePath> sourceFiles, IORelay environment) {
		if (sourceFiles.size() != 1)
			throw new IllegalArgumentException(
					"Can only compile one source file at a time.");

		relFileName = FileCommands.dropExtension(sourceFiles.iterator().next()
				.getRelativePath());
		isHeader = isHeaderModule(relFileName);
		String extension = getFileExtension(isHeader);

		outFile = environment.createOutPath(relFileName + "." + extension);
	}

	@Override
	public List<String> processBaseDecl(IStrategoTerm toplevelDecl)
			throws IOException {
		if (!isMain && containsMain(toplevelDecl)) {
			isMain = true;
		}

		if (isApplication(toplevelDecl, "CDependency")) {
			List<String> additionalDeps = new ArrayList<String>();
			String module = getModulePathOfImport(toplevelDecl);
			String extension = getFileExtension(module);
			deps.add(module + "." + extension);
			additionalDeps.add(module);

			return additionalDeps;
		}

		String text = null;
		try {
			text = prettyPrint(toplevelDecl);
		} catch (NullPointerException e) {
			ATermCommands.setErrorMessage(toplevelDecl,
					"pretty printing C failed");
		}
		if (text != null) {
			if (isApplication(toplevelDecl, "PragmaOnce"))
				pragmas.add(text);
			else
				body.add(text);
		}

		return Collections.emptyList();
	}

	@Override
	public String getModulePathOfImport(IStrategoTerm toplevelDecl) {
		String name = null;
		if (isApplication(toplevelDecl, "CExtensionImport")
				|| isApplication(toplevelDecl, "CDependency"))
			name = prettyPrint(toplevelDecl.getSubterm(0));
		return name;
	}

	@Override
	public void processModuleImport(IStrategoTerm toplevelDecl)
			throws IOException {
		String modulePath = getModulePathOfImport(toplevelDecl);
		String namespace = getNamespace();

		String prettyPrint = prettyPrint(toplevelDecl);
		StringBuilder sb = new StringBuilder(prettyPrint);
		String extension = getFileExtension(modulePath);
		sb.insert(prettyPrint.lastIndexOf("\""), "." + extension);

		if (!namespace.isEmpty() && modulePath.startsWith(namespace)) {
			sb.delete(sb.indexOf("\"") + 1, sb.lastIndexOf(Environment.sep) + 1);
		}
		prettyPrint = sb.toString();

		imports.add(prettyPrint);
	}

	@Override
	public String getExtensionName(IStrategoTerm decl) throws IOException {
		IStrategoTerm cExtensionHead = getApplicationSubterm(decl,
				"CExtension", 0);
		IStrategoTerm id = getApplicationSubterm(cExtensionHead,
				"CExtensionHead", 0);
		String extensionName = prettyPrint(id);
		return extensionName;
	}

	public String prettyPrint(IStrategoTerm term) {
		if (ppTable == null)
			ppTable = ATermCommands.readPrettyPrintTable(lang.ensureFile(
					"org/sugarj/languages/C.pp").getAbsolutePath());

		String prettyPrint = ATermCommands.prettyPrint(ppTable, term, interp);
		return prettyPrint;
	}

	@Override
	public List<Path> compile(List<Path> outFiles, Path bin,
			List<Path> includePaths) throws IOException {
		if (outFiles.size() != 1)
			throw new IllegalArgumentException(
					"Can only compile one source file at a time.");
		List<Path> generatedFiles = new ArrayList<Path>();
		Path outFile = outFiles.get(0);
		generatedFiles.addAll(CCommands.writeDependencyFile(outFile, imports,
				deps));
		generatedFiles
				.addAll(CCommands.gcc(outFile, bin, includePaths, isMain));

		return generatedFiles;
	}

	@Override
	public boolean isModuleExternallyResolvable(String relModulePath) {
		return false;
	}

	@Override
	public IStrategoTerm getExtensionBody(IStrategoTerm decl) {
		IStrategoTerm extensionBody = getApplicationSubterm(decl, "CExtension",
				1);
		return getApplicationSubterm(extensionBody, "CExtensionBody", 0);
	}

	private String getFileExtension(boolean isHeader) {
		String extension = isHeader ? lang.getHeaderFileExtension() : lang
				.getBaseFileExtension();
		return extension;
	}

	private String getFileExtension(String moduleName) {
		boolean isHeader = isHeaderModule(moduleName);
		return getFileExtension(isHeader);
	}

	private boolean isHeaderModule(String moduleName) {
		boolean isHeader = moduleName.endsWith(lang.getHeaderSuffix());
		return isHeader;
	}

	private boolean containsMain(IStrategoTerm decl) {
		IStrategoTerm funDef = mayFind("FunDef", decl);
		if (funDef != null) {
			IStrategoTerm declarator = funDef.getSubterm(1);
			IStrategoTerm declParams = getApplicationSubterm(declarator,
					"Declarator", 1);
			String id = declParams.getSubterm(0).getSubterm(0).toString();

			if (id.equals("\"main\"")) {
				return true;
			}
		}
		return false;
	}
}
