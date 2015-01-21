package org.sugarj;

import static org.sugarj.common.ATermCommands.getApplicationSubterm;
import static org.sugarj.common.ATermCommands.isApplication;
import static org.sugarj.util.TermFinder.find;

import java.io.IOException;
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
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CProcessor extends AbstractBaseProcessor {

	private static final long serialVersionUID = 2057395343737713876L;

	private String moduleHeader;
	private List<String> imports = new LinkedList<String>();
	private List<String> body = new LinkedList<String>();

	private Path outFile;
	private String namespaceName;
	private boolean isHeader;

	private IStrategoTerm ppTable;

	@Override
	public String getGeneratedSource() {
		if (body.isEmpty()) {
			return "";
		}

		StringBuilder sourceBuilder = new StringBuilder();

		if (moduleHeader != null) {
			sourceBuilder.append(moduleHeader).append("\n\n");
		}

		sourceBuilder.append(StringCommands.printListSeparated(imports, "\n"))
				.append("\n\n");
		sourceBuilder.append(StringCommands.printListSeparated(body, "\n"));
		String source = sourceBuilder.toString();

		return source;
	}

	@Override
	public Path getGeneratedSourceFile() {
		updateOutFile();
		return outFile;
	}

	private void updateOutFile() {
		CLanguage lang = getLanguage();
		String baseFileExtension = lang.getBaseFileExtension();
		String outPath = outFile.getAbsolutePath();

		if (isHeader && outPath.endsWith(baseFileExtension)) {
			StringBuilder updatedPath = new StringBuilder(outPath);
			updatedPath.delete(outPath.length() - baseFileExtension.length()
					- 1, outPath.length());
			updatedPath.append(".");
			updatedPath.append(lang.getHeaderFileExtension());
			outFile = new AbsolutePath(updatedPath.toString());
		}
	}

	@Override
	public String getNamespace() {
		return namespaceName;
	}

	@Override
	public CLanguage getLanguage() {
		return CLanguage.getInstance();
	}

	@Override
	public void init(Set<RelativePath> sourceFiles, Environment environment) {
		if (sourceFiles.size() != 1)
			throw new IllegalArgumentException(
					"Can only compile one source file at a time.");

		isHeader = false;

		String firstFileName = FileCommands.dropExtension(sourceFiles
				.iterator().next().getRelativePath());

		outFile = environment.createOutPath(firstFileName + "."
				+ CLanguage.getInstance().getBaseFileExtension());
	}

	@Override
	public List<String> processBaseDecl(IStrategoTerm toplevelDecl)
			throws IOException {
		if (!isHeader && getLanguage().isHeaderFlag(toplevelDecl)) {
			isHeader = true;
			return Collections.emptyList();
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
		if (isApplication(toplevelDecl, "CExtensionImport"))
			name = prettyPrint(toplevelDecl.getSubterm(0));
		if (name != null && name.contains(".")) {
			String withoutExtension = name.substring(0, name.indexOf('.'));
			return withoutExtension;
		}
		return name;
	}

	@Override
	public void processModuleImport(IStrategoTerm toplevelDecl)
			throws IOException {
		imports.add(prettyPrint(toplevelDecl));
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
			ppTable = ATermCommands.readPrettyPrintTable(getLanguage()
					.ensureFile("org/sugarj/languages/C.pp").getAbsolutePath());

		String prettyPrint = ATermCommands.prettyPrint(ppTable, term, interp);
		return prettyPrint;
	}

	@Override
	public List<Path> compile(List<Path> outFiles, Path bin,
			List<Path> includePaths) throws IOException {
		return CCommands.gcc(outFiles, bin, includePaths);
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

		IStrategoTerm funDef = find("FunDef", decl);

		if (funDef != null) {
			IStrategoTerm declarator = funDef.getSubterm(1);
			IStrategoTerm declParams = declarator.getSubterm(1);
			String id = declParams.getSubterm(0).getSubterm(0).toString();

			if (id.equals("main")) {
				return true;
			}
		}

		return false;
	}
}
