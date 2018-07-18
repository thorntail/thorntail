package io.thorntail.jpa.impl.opentracing;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

/**
 * Created by bob on 2/27/18.
 */
public class TracedTypedQuery<T> extends TracedQuery implements TypedQuery<T> {

    public TracedTypedQuery(TraceInfo traceInfo, String name, Class<T> resultClass, TypedQuery<T> delegate) {
        super( traceInfo.withDecorator((span) -> {
            span.setTag("class", resultClass.getName());
        }), delegate );
        this.name = name;
        this.resultClass = resultClass;
    }

    protected TypedQuery<T> getDelegate() {
        return (TypedQuery<T>) super.getDelegate();
    }

    protected String operationName(String op) {
        return this.name + "/" + op;
    }

    @Override
    public List getResultList() {
        return traceInfo().trace(operationName("getResultList"), () -> getDelegate().getResultList());
    }

    @Override
    public T getSingleResult() {
        return traceInfo().trace(operationName("getSingleResult"), () -> getDelegate().getSingleResult());
    }

    @Override
    public int executeUpdate() {
        return traceInfo().trace(operationName("executeUpdate"), () -> getDelegate().executeUpdate());
    }

    @Override
    public TypedQuery<T> setMaxResults(int maxResult) {
        return getDelegate().setMaxResults(maxResult);
    }

    @Override
    public int getMaxResults() {
        return getDelegate().getMaxResults();
    }

    @Override
    public TypedQuery<T> setFirstResult(int startPosition) {
        return getDelegate().setFirstResult(startPosition);
    }

    @Override
    public int getFirstResult() {
        return getDelegate().getFirstResult();
    }

    @Override
    public TypedQuery<T> setHint(String hintName, Object value) {
        return getDelegate().setHint(hintName, value);
    }

    @Override
    public Map<String, Object> getHints() {
        return getDelegate().getHints();
    }

    @Override
    public <J> TypedQuery<T> setParameter(Parameter<J> param, J value) {
        return getDelegate().setParameter(param, value);
    }

    @Override
    public TypedQuery<T> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        return getDelegate().setParameter(param, value, temporalType);
    }

    @Override
    public TypedQuery<T> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        return getDelegate().setParameter(param, value, temporalType);
    }

    @Override
    public TypedQuery<T> setParameter(String name, Object value) {
        return getDelegate().setParameter(name, value);
    }

    @Override
    public TypedQuery<T> setParameter(String name, Calendar value, TemporalType temporalType) {
        return getDelegate().setParameter(name, value, temporalType);
    }

    @Override
    public TypedQuery<T> setParameter(String name, Date value, TemporalType temporalType) {
        return getDelegate().setParameter(name, value, temporalType);
    }

    @Override
    public TypedQuery<T> setParameter(int position, Object value) {
        return getDelegate().setParameter(position, value);
    }

    @Override
    public TypedQuery<T> setParameter(int position, Calendar value, TemporalType temporalType) {
        return getDelegate().setParameter(position, value, temporalType);
    }

    @Override
    public TypedQuery<T> setParameter(int position, Date value, TemporalType temporalType) {
        return getDelegate().setParameter(position, value, temporalType);
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return getDelegate().getParameters();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return getDelegate().getParameter(name);
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return getDelegate().getParameter(name, type);
    }

    @Override
    public Parameter<?> getParameter(int position) {
        return getDelegate().getParameter(position);
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return getDelegate().getParameter(position, type);
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        return getDelegate().isBound(param);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        return getDelegate().getParameterValue(param);
    }

    @Override
    public Object getParameterValue(String name) {
        return getDelegate().getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
        return getDelegate().getParameterValue(position);
    }

    @Override
    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        return getDelegate().setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return getDelegate().getFlushMode();
    }

    @Override
    public TypedQuery<T> setLockMode(LockModeType lockMode) {
        return getDelegate().setLockMode(lockMode);
    }

    @Override
    public LockModeType getLockMode() {
        return getDelegate().getLockMode();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return getDelegate().unwrap(cls);
    }

    private final String name;

    private final Class<T> resultClass;
}
