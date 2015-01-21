package org.sugarj.util;

import static org.sugarj.common.ATermCommands.isApplication;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class TermFinder {
	public static class TermNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 965353767684706976L;
	}

	public static IStrategoTerm mayFind(String cons, IStrategoTerm root) {
		if (isApplication(root, cons)) {
			return root;
		}

		IStrategoTerm result = null;
		for (IStrategoTerm subterm : root) {
			result = mayFind(cons, subterm);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	public static IStrategoTerm find(String cons, IStrategoTerm root)
			throws TermNotFoundException {
		IStrategoTerm result = mayFind(cons, root);
		if (result == null) {
			throw new TermNotFoundException();
		}
		return result;
	}

	public static List<IStrategoTerm> select(String cons, IStrategoTerm root) {
		List<IStrategoTerm> results = new ArrayList<IStrategoTerm>();
		if (isApplication(root, cons)) {
			results.add(root);
		}

		for (IStrategoTerm subterm : root) {
			results.addAll(select(cons, subterm));
		}

		return results;
	}
}
