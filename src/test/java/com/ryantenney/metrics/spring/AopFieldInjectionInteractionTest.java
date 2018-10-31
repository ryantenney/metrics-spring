/**
 * Copyright © 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryantenney.metrics.spring;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metric;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:aop-field-injection-interaction.xml")
public class AopFieldInjectionInteractionTest {

	@Autowired
	private MetricRegistry metricRegistry;
	@Autowired
	private TestAspectTarget target;

	@Test
	public void testFieldInjectionShouldNotCauseErrorWhenTargetIsAopProxy() throws Exception {
		// verify that AOP interception is working
		assertThat(target.targetMethod(), is(5));

		assertThat(metricRegistry.getCounters(), hasKey("targetCounter"));
		assertThat(metricRegistry.getCounters().get("targetCounter").getCount(), is(1L));
	}

}

@Aspect
class TestAspectWrapper {

	@Around("execution(* com.ryantenney.metrics.spring.TestAspectTarget.targetMethod())")
	public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		joinPoint.proceed();
		return 5;
	}

}

interface TestAspectTarget {

	public int targetMethod();

}

class TestAspectTargetImpl implements TestAspectTarget {

	@Metric(name = "targetCounter", absolute = true)
	private Counter counter;

	@Override
	public int targetMethod() {
		this.counter.inc();
		return 3;
	}

}
