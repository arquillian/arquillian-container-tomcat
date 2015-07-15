package org.jboss.arquillian.container.tomcat.embedded;

import java.lang.reflect.Constructor;

import org.apache.catalina.util.ContextName;

final class CompatUtils {
	private CompatUtils() {}

	public static ContextName createContextName(String name) {
		try {
			Constructor<ContextName> constructor = null;

			// Tomcat >= 7.0.52
			constructor = getConstructor(ContextName.class, String.class, Boolean.TYPE);
			if(constructor != null) {
				return constructor.newInstance(name, true);
			}

			// Tomcat < 7.0.52
			constructor = getConstructor(ContextName.class, String.class);
			if(constructor != null) {
				return constructor.newInstance(name);
			}
		} catch(Exception e) {
			throw new RuntimeException("Could not invoke ContextName constructor", e);
		}
		throw new RuntimeException("Could not determine correct ContextName version for current Tomcat version");
	}

	private static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... args) {
		try {
			return type.getDeclaredConstructor(args);
		} catch (Exception e) {
			return null;
		}
	}
}
