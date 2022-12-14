package org.lucee.extension.image.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.stream.ImageInputStream;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.coder.Coder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;

public class CommonUtil {

	public static final short UNDEFINED_NODE = -1;
	private static final String _8220 = String.valueOf((char) 8220);

	private static Map<Collection.Key, Coll> members;
	private static BIF GetApplicationSettings;

	public static String unwrap(String str) {
		if (str == null) return "";
		str = str.trim();
		if (str.length() == 0) return "";

		if ((str.startsWith("\"") || str.startsWith(_8220)) && (str.endsWith("\"") || str.endsWith(_8220))) // #8220 and #8221 are left and right "double quotes"
			str = str.substring(1, str.length() - 1);
		if (str.startsWith("'") && str.endsWith("'")) str = str.substring(1, str.length() - 1);
		return str;
	}

	public static String removeWhiteSpace(String str) {
		if (str == null || str.length() == 0) return str;
		StringBuilder sb = new StringBuilder();
		char[] carr = str.trim().toCharArray();
		for (int i = 0; i < carr.length; i++) {
			if (!Character.isWhitespace(carr[i])) sb.append(carr[i]);
		}
		return sb.toString();
	}

	public static Object getVariableEL(PageContext pc, String var, Object defaultValue) {
		try {
			Class<?> clazz = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.interpreter.VariableInterpreter");
			Method m = clazz.getMethod("getVariableEL", new Class[] { PageContext.class, String.class, Object.class });
			return m.invoke(null, new Object[] { pc, var, defaultValue });
		}
		catch (Exception e) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			throw eng.getExceptionUtil().createPageRuntimeException(eng.getCastUtil().toPageException(e));
		}
	}

	public static synchronized ArrayList<Node> getChildNodes(Node node, short type, String filter) {
		ArrayList<Node> rtn = new ArrayList<Node>();
		NodeList nodes = node.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			try {
				n = nodes.item(i);
				if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
					if (filter == null || filter.equals(n.getLocalName())) rtn.add(n);
				}
			}
			catch (Exception t) {
			}
		}
		return rtn;
	}

	public static Object call(PageContext pc, Object coll, Collection.Key methodName, Object[] args, short[] types, String[] strTypes) throws PageException {
		try {
			Class<?> clazz = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.type.util.MemberUtil");
			Method m = clazz.getMethod("call", new Class[] { PageContext.class, Object.class, Collection.Key.class, Object[].class, short[].class, String[].class });
			return m.invoke(null, new Object[] { pc, coll, methodName, args, types, strTypes });
		}
		catch (Exception e) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			throw eng.getExceptionUtil().createPageRuntimeException(eng.getCastUtil().toPageException(e));
		}
	}

	public static Object callWithNamedValues(PageContext pc, Object coll, Collection.Key methodName, Struct args, short type, String strType) throws PageException {
		try {
			Class<?> clazz = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.type.util.MemberUtil");
			Method m = clazz.getMethod("callWithNamedValues", new Class[] { PageContext.class, Object.class, Collection.Key.class, Struct.class, short.class, String.class });
			return m.invoke(null, new Object[] { pc, coll, methodName, args, type, strType });
		}
		catch (Exception e) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			throw eng.getExceptionUtil().createPageRuntimeException(eng.getCastUtil().toPageException(e));
		}
	}

	public static String ContractPath(PageContext pc, String abs) throws PageException {
		try {
			BIF bif = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "lucee.runtime.functions.system.ContractPath");
			return (String) bif.invoke(pc, new Object[] { abs });
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static String[] trim(String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
		}
		return arr;
	}

	/**
	 * return the size of the Resource, other than method length of Resource this mthod return the size
	 * of all files in a directory
	 * 
	 * @param collectionDir
	 * @return
	 */
	public static long getRealSize(Resource res, ResourceFilter filter) {
		if (res.isFile()) {
			return res.length();
		}
		else if (res.isDirectory()) {
			long size = 0;
			Resource[] children = filter == null ? res.listResources() : res.listResources(filter);
			for (int i = 0; i < children.length; i++) {
				size += getRealSize(children[i], filter);
			}
			return size;
		}

		return 0;
	}

	public static Map<Collection.Key, Coll> getMembers(PageContext pc) throws PageException {
		if (members == null) {
			Cast cast = CFMLEngineFactory.getInstance().getCastUtil();
			members = new HashMap<Collection.Key, Coll>();
			ConfigWeb config = pc.getConfig();
			Object[] flds = getFLDs(config, 1);
			Map funcs;
			Iterator it;
			Object func;
			String[] names;
			boolean chaining;
			BIF bif;
			Coll coll;
			for (int i = 0; i < flds.length; i++) {
				funcs = getFunctions(flds[i]);
				it = funcs.values().iterator();
				while (it.hasNext()) {
					func = it.next();
					if (getMemberType(func) == Image.TYPE_IMAGE) {
						names = getMemberNames(func);
						if (names != null && names.length > 0) {
							coll = new Coll(getBIF(func), getMemberChaining(func));
							for (String name: names) {
								members.put(cast.toKey(name), coll);
							}
						}
					}
				}
			}
		}
		return members;
	}

	private static boolean getMemberChaining(Object func) throws PageException {
		try {
			Method m = func.getClass().getMethod("getMemberChaining", new Class[] {});
			return ((Boolean) m.invoke(func, new Object[] {})).booleanValue();
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static BIF getBIF(Object func) throws PageException {
		try {
			Method m = func.getClass().getMethod("getBIF", new Class[] {});
			return (BIF) m.invoke(func, new Object[] {});
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static String[] getMemberNames(Object func) throws PageException {
		try {
			Method m = func.getClass().getMethod("getMemberNames", new Class[] {});
			return (String[]) m.invoke(func, new Object[] {});
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static short getMemberType(Object func) throws PageException {
		try {
			Method m = func.getClass().getMethod("getMemberType", new Class[] {});
			return ((Short) m.invoke(func, new Object[] {})).shortValue();
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static Map getFunctions(Object fld) throws PageException, RuntimeException {
		try {
			Method m = fld.getClass().getMethod("getFunctions", new Class[] {});
			return (Map) m.invoke(fld, new Object[] {});
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static Object[] getFLDs(ConfigWeb config, int dialect) throws PageException {
		try {
			Method m = config.getClass().getMethod("getFLDs", new Class[] { int.class });
			return (Object[]) m.invoke(config, new Object[] { dialect });
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	public static class Coll {

		public final BIF bif;
		public final boolean memberChaining;

		public Coll(BIF bif, boolean memberChaining) {
			this.bif = bif;
			this.memberChaining = memberChaining;
		}

	}

	public static void close(ImageInputStream iis) {
		if (iis == null) return;
		try {
			iis.close();
		}
		catch (Exception e) {
		}
	}

	public static Set<String> getCoders(StringBuilder sb, PageContext pc) {
		Set<String> result = null;
		try {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			if (pc == null) pc = eng.getThreadPageContext();
			if (pc == null) return null;
			if (GetApplicationSettings == null) {
				GetApplicationSettings = eng.getClassUtil().loadBIF(pc, "lucee.runtime.functions.system.GetApplicationSettings");
			}
			Struct sct = (Struct) GetApplicationSettings.invoke(pc, new Object[] { Boolean.TRUE });
			Object o = sct.get("image", null);
			if (o instanceof Struct) {
				Struct image = (Struct) o;
				// type
				o = image.get("coder", null);
				if (o == null) image.get("coders", null);

				if (o != null && eng.getDecisionUtil().isCastableToArray(o)) {
					String[] coders = eng.getListUtil().toStringArray(eng.getCastUtil().toArray(o));
					for (String c: coders) {
						if (Util.isEmpty(c, true)) continue;
						if (result == null) result = new HashSet<>();
						sb.append(c = c.trim().toLowerCase()).append(';');
						result.add(c);
					}
				}
			}
		}
		catch (Exception e) {
			Coder.log(pc);
		}

		return result;
	}
}
