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
import org.sugarj.common.Environment;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CProcessor extends AbstractBaseProcessor {

	private static final long serialVersionUID = 2057395343737713876L;

	private CLanguage lang = CLanguage.getInstance();
	private List<String> imports = new LinkedList<String>();
	private List<String> deps = new LinkedList<String>();
	private List<String> body = new LinkedList<String>();

	private Path outFile;
	private String relFileName;
	private boolean isMain;

	private IStrategoTerm ppTable;

	@Override
	public String getGeneratedSource() {
		if (body.isEmpty())
			return "";

		StringBuilder sourceBuilder = new StringBuilder();
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
	public void init(Set<RelativePath> sourceFiles, Environment environment) {
		if (sourceFiles.size() != 1)
			throw new IllegalArgumentException(
					"Can only compile one source file at a time.");

		relFileName = FileCommands.dropExtension(sourceFiles.iterator().next()
				.getRelativePath());

		outFile = environment.createOutPath(relFileName + "."
				+ lang.getBaseFileExtension());
	}

	@Override
	public List<String> processBaseDecl(IStrategoTerm toplevelDecl)
			throws IOException {
		if (!isMain && containsMain(toplevelDecl)) {
			isMain = true;
		}
		String text = null;
		try {
			text = prettyPrint(toplevelDecl);
		} catch (NullPointerException e) {
			ATermCommands.setErrorMessage(toplevelDecl,
					"pretty printing C failed");
		}
		if (text != null)
			body.add(text);

		return Collections.emptyList();
	}

	@Override
	public String getModulePathOfImport(IStrategoTerm toplevelDecl) {
		String name = null;
		if (isApplication(toplevelDecl, "CExtensionImport")
				|| isApplication(toplevelDecl, "CForwarding"))
			name = prettyPrint(toplevelDecl.getSubterm(0));
		if (!name.contains("/")) {
			name = getNamespace() + Environment.sep + name;
		}
		return name;
	}

	@Override
	public void processModuleImport(IStrategoTerm toplevelDecl)
			throws IOException {
		if (isApplication(toplevelDecl, "CForwarding")) {
			deps.add(getModulePathOfImport(toplevelDecl) + "."
					+ lang.getBaseFileExtension());
			return;
		}
		String prettyPrint = prettyPrint(toplevelDecl);
		StringBuilder sb = new StringBuilder(prettyPrint);
		String extension = lang.getBaseFileExtension();
		sb.insert(prettyPrint.lastIndexOf("\""), "." + extension);
		prettyPrint = sb.toString();

		imports.add(prettyPrint);
	}

	@Override
	public String getExtensionName(IStrategoTerm decl) throws IOException {
		IStrategoTerm cExtensionHead = getApplicationSubterm(decl,
				"CExtension", 0);
		IStrategoTerm cId = getApplicationSubterm(cExtensionHead,
				"CExtensionHead", 0);
		String extensionName = prettyPrint(cId);
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

	private boolean containsMain(IStrategoTerm decl) {
		IStrategoTerm funDef = mayFind("FunDef", decl);
		if (funDef != null) {
			IStrategoTerm declarator = funDef.getSubterm(1);
			IStrategoTerm declParams = declarator.getSubterm(1);
			String id = declParams.getSubterm(0).getSubterm(0).toString();

			if (id.equals("\"main\"")) {
				return true;
			}
		}
		return false;
	}
}
