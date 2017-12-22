package org.lucee.extension.image.functions;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Excepton;

public abstract class FunctionSupport extends BIF {
	
	static CFMLEngine eng;
	static Cast cast;
	static Excepton exp;

	static {
		eng = CFMLEngineFactory.getInstance();
		cast=eng.getCastUtil();
		exp=eng.getExceptionUtil();
	}
}
