package com.ryantenney.metrics.spring;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

public class BeanPropertyNamingStrategy extends AbstractNamingStrategy {

	@Override
	protected String chooseName(String explicitName, boolean absolute, Class<?> klass, String beanName, Member member, String... suffixes) {
		if (explicitName != null && !explicitName.isEmpty()) {
			if (absolute) {
				return explicitName;
			}
			return name(beanName, explicitName);
		}
		String memberName = member.getName();
		if (member instanceof Method) {
			if (memberName.startsWith("get")) {
				memberName = memberName.substring("get".length());
			}
			else if (memberName.startsWith("is")) {
				memberName = memberName.substring("is".length());
			}
		}
		return name(name(beanName, memberName), suffixes);
	}

}
