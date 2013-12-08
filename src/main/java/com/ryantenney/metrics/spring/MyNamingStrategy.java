package com.ryantenney.metrics.spring;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.InjectMetric;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author orantius
 * @version $Id$
 * @since 12/8/13
 */
public class MyNamingStrategy implements NamingStrategy {


    @Override
    public String forTimedMethod(Class<?> klass, String beanName, Member member, Timed annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member);
    }

    @Override
    public String forMeteredMethod(Class<?> klass, String beanName, Member member, Metered annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member);
    }

    @Override
    public String forGauge(Class<?> klass, String beanName, Member member, Gauge annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member);
    }

    @Override
    public String forCachedGauge(Class<?> klass, String beanName, Member member, CachedGauge annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member);
    }

    @Override
    public String forExceptionMeteredMethod(Class<?> klass, String beanName, Member member, ExceptionMetered annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member, ExceptionMetered.DEFAULT_NAME_SUFFIX);
    }

    @Override
    public String forCountedMethod(Class<?> klass, String beanName, Member member, Counted annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member);
    }

    @Override
    public String forInjectMetricField(Class<?> klass, String beanName, Member member, InjectMetric annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, beanName, member);
    }

    static String chooseName(String explicitName, boolean absolute, Class<?> klass, String beanName, Member member, String... suffixes) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return name(beanName, explicitName);
        }
        String memberName = member.getName();
        if (member instanceof Method) {
            if(memberName.startsWith("get")) memberName = memberName.substring("get".length());
            if(memberName.startsWith("is")) memberName = memberName.substring("is".length());
        }
        return name(name(beanName, memberName), suffixes);
    }

}
