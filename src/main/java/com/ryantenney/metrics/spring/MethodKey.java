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
		result = prime * result + name.hashCode();
		result = prime * result + Arrays.hashCode(parameterTypes);
		result = prime * result + returnType.hashCode();
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
		final MethodKey other = (MethodKey) obj;
		return name.equals(other.name)
			&& returnType.equals(other.returnType)
			&& Arrays.equals(parameterTypes, other.parameterTypes);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(returnType.getSimpleName());
		sb.append(' ');
		sb.append(name);
		sb.append('(');
		boolean firstParam = true;
		for (Class<?> parameterType : parameterTypes) {
			if (firstParam) {
				firstParam = false;
			}
			else {
				sb.append(", ");
			}
			sb.append(parameterType.getSimpleName());
		}
		sb.append(')');
		return sb.toString();
	}

}
