package org.sugarj;

import static org.sugarj.common.ATermCommands.getApplicationSubterm;
import static org.sugarj.common.ATermCommands.isApplication;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.Environment;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CProcessor extends AbstractBaseProcessor {

	private static final long serialVersionUID = 2057395343737713876L;

	private String moduleHeader;
	private List<String> imports = new LinkedList<String>();
	private List<String> body = new LinkedList<String>();

	private Path outFile;
	private String namespaceName;

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
		return outFile;
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

		outFile = environment.createOutPath(FileCommands
				.dropExtension(sourceFiles.iterator().next().getRelativePath())
				+ "." + CLanguage.getInstance().getBaseFileExtension());
	}

	@Override
	public List<String> processBaseDecl(IStrategoTerm toplevelDecl)
			throws IOException {

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
		if (isApplication(toplevelDecl, "Include")
				|| isApplication(toplevelDecl, "StdInclude"))
			name = prettyPrint(toplevelDecl.getSubterm(0));

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
		IStrategoTerm scalaId = getApplicationSubterm(cExtensionHead,
				"CExtensionHead", 0);
		String extensionName = prettyPrint(scalaId);
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
		return Collections.emptyList(); // TODO
	}

	@Override
	public boolean isModuleExternallyResolvable(String relModulePath) {
		return false; // TODO
	}

	@Override
	public IStrategoTerm getExtensionBody(IStrategoTerm decl) {
		IStrategoTerm extensionBody = getApplicationSubterm(decl, "CExtension",
				1);
		return getApplicationSubterm(extensionBody, "CExtensionBody", 0);
	}
}
