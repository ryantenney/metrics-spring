/**
 * Copyright (C) 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
