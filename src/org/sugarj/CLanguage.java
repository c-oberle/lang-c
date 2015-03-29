package org.sugarj;

import static org.sugarj.common.ATermCommands.isApplication;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.path.Path;

public class CLanguage extends AbstractBaseLanguage {

	private CLanguage() {
	}

	private static CLanguage instance = new CLanguage();

	public static CLanguage getInstance() {
		return instance;
	}

	@Override
	public AbstractBaseProcessor createNewProcessor() {
		return new CProcessor();
	}

	@Override
	public String getLanguageName() {
		return "C";
	}

	@Override
	public String getVersion() {
		return "c-99";
	}

	@Override
	public String getBinaryFileExtension() {
		return "o";
	}

	@Override
	public String getSugarFileExtension() {
		return "sugc";
	}

	@Override
	public String getBaseFileExtension() {
		return "c";
	}

	public String getHeaderFileExtension() {
		return "h";
	}

	public String getHeaderSuffix() {
		return "_" + getHeaderFileExtension();
	}

	@Override
	public List<Path> getPackagedGrammars() {
		List<Path> grammars = new LinkedList<Path>(super.getPackagedGrammars());
		grammars.add(ensureFile("org/sugarj/languages/SugarC.def"));
		grammars.add(ensureFile("org/sugarj/languages/C.def"));
		return Collections.unmodifiableList(grammars);
	}

	@Override
	public Path getInitEditor() {
		return ensureFile("org/sugarj/c/init/initEditor.serv");
	}

	@Override
	public String getInitEditorModuleName() {
		return "org/sugarj/c/init/initEditor";
	}

	@Override
	public Path getInitGrammar() {
		return ensureFile("org/sugarj/c/init/initGrammar.sdf");
	}

	@Override
	public String getInitGrammarModuleName() {
		return "org/sugarj/c/init/initGrammar";
	}

	@Override
	public Path getInitTrans() {
		return ensureFile("org/sugarj/c/init/initTrans.str");
	}

	@Override
	public String getInitTransModuleName() {
		return "org/sugarj/c/init/initTrans";
	}

	@Override
	public boolean isExtensionDecl(IStrategoTerm decl) {
		return isApplication(decl, "CExtension");
	}

	@Override
	public boolean isImportDecl(IStrategoTerm decl) {
		return isApplication(decl, "CExtensionImport")
				|| isApplication(decl, "CDependency");
	}

	@Override
	public boolean isBaseDecl(IStrategoTerm decl) {
		return isApplication(decl, "ExtDec") || isApplication(decl, "FunDef")
				|| isApplication(decl, "Include")
				|| isApplication(decl, "StdInclude")
				|| isApplication(decl, "PragmaOnce");
	}

	@Override
	public boolean isPlainDecl(IStrategoTerm decl) {
		return false;
	}

}
