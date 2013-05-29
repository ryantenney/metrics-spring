package com.ryantenney.metrics.spring;

import java.lang.reflect.Method;
import java.util.Arrays;

class MethodKey {
	private final String name;
	private final Class<?> returnType;
	private final Class<?>[] parameterTypes;
	private final int hashCode;

	public static MethodKey forMethod(Method method) {
		return new MethodKey(method);
	}

	private MethodKey(Method method) {
		this.name = method.getName();
		this.returnType = method.getReturnType();
		this.parameterTypes = method.getParameterTypes();
		this.hashCode = computeHashCode();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private int computeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(parameterTypes);
		result = prime * result + (returnType == null ? 0 : returnType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MethodKey other = (MethodKey) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		if (!Arrays.equals(parameterTypes, other.parameterTypes)) {
			return false;
		}
		if (returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		}
		else if (!returnType.equals(other.returnType)) {
			return false;
		}
		return true;
	}

}
