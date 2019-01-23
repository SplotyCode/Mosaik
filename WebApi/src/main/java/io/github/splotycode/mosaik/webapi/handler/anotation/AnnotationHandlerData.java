package io.github.splotycode.mosaik.webapi.handler.anotation;

import io.github.splotycode.mosaik.annotations.AnnotationHelper;
import io.github.splotycode.mosaik.util.Pair;
import io.github.splotycode.mosaik.webapi.handler.UrlPattern;
import io.github.splotycode.mosaik.webapi.handler.anotation.check.*;
import io.github.splotycode.mosaik.webapi.handler.anotation.handle.UseResolver;
import io.github.splotycode.mosaik.webapi.handler.anotation.parameter.ParameterResolver;
import io.github.splotycode.mosaik.webapi.request.Request;
import io.github.splotycode.mosaik.webapi.request.RequestHeaders;
import io.github.splotycode.mosaik.webapi.response.content.ResponseContent;
import io.github.splotycode.mosaik.webapi.server.AbstractWebServer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

@EqualsAndHashCode
@Getter
@Setter
public class AnnotationHandlerData {

    private UrlPattern mapping = null;
    private int priority;
    private String method = null;
    private boolean costomMethod = false;
    private String host;
    private List<String> neededGet = new ArrayList<>(), neededPost = new ArrayList<>();
    private HashMap<String, String> getMustBe = new HashMap<>(), postMustBe = new HashMap<>();
    private Throwable loadingError = null;

    protected List<ParameterResolver> costomParameterResolvers = new ArrayList<>();

    public AnnotationHandlerData(Annotation[] annotations) {
        priority = AnnotationHelper.getPriority(annotations);
        for (Annotation annotation : annotations) {
            if (annotation instanceof Mapping) {
                mapping = new UrlPattern(((Mapping) annotation).value());
            } else if (annotation instanceof NeedGetMethod) {
                setMethod("GET");
            } else if (annotation instanceof NeedPostMethod) {
                setMethod("POST");
            } else if (annotation instanceof NeedMethod) {
                setMethod(((NeedMethod) annotation).method().toUpperCase());
                costomMethod = true;
            } else if (annotation instanceof NeedGetParameter) {
                setMethod("GET");
                Collections.addAll(neededGet, ((NeedGetParameter) annotation).parameters());
            } else if (annotation instanceof NeedPostParameter) {
                setMethod("POST");
                Collections.addAll(neededPost, ((NeedPostParameter) annotation).parameters());
            } else if (annotation instanceof GetMustBe) {
                setMethod("GET");
                GetMustBe mustBeAnnotation = (GetMustBe) annotation;
                getMustBe.put(mustBeAnnotation.parameter(), mustBeAnnotation.value());
            } else if (annotation instanceof PostMustBe) {
                setMethod("POST");
                PostMustBe mustBeAnnotation = (PostMustBe) annotation;
                postMustBe.put(mustBeAnnotation.parameter(), mustBeAnnotation.value());
            } else if (annotation instanceof AddTransformer) {
                for (Class<? extends ParameterResolver> transformer : ((AddTransformer) annotation).value()) {
                    try {
                        costomParameterResolvers.add(transformer.newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        loadingError = e;
                    }
                }
            } else if (annotation instanceof Host) {
                host = (((Host) annotation).value()).trim().toLowerCase(Locale.ENGLISH);
            }
        }
    }

    private void setMethod(String method) {
        if (this.method != null && !method.equals(this.method))
            throw new IllegalStateException("Can not force two different methods (" + priority + " and " + this.priority);
        this.method = method;
    }

    public boolean valid(Request request) {
        String host = request.getHeader(RequestHeaders.HOST);
        if (host == null) {
            if (this.host != null) return false;
        } else if (!host.trim().toLowerCase(Locale.ENGLISH).equals(host)) {
             return false;
        }
        if (mapping != null && !mapping.match(request.getPath()).isMatch()) return false;
        if (method != null && (costomMethod ? request.getMethod().getMethod().matches(method) : request.getMethod().getMethod().equals(method))) return false;
        for (String get : neededGet)
            if (!request.getGet().containsKey(get))
                return false;

        for (String get : neededPost)
            if (!request.getPost().containsKey(get))
                return false;

        for (Map.Entry<String, String> pair : getMustBe.entrySet()) {
            if (!request.getGet().containsKey(pair.getKey()) || !request.getFirstGetParameter(pair.getKey()).equals(pair.getValue()))
                return false;
        }

        for (Map.Entry<String, String> pair : postMustBe.entrySet()) {
            if (!request.getPost().containsKey(pair.getKey()) || !request.getFirstPostParameter(pair.getKey()).equals(pair.getValue()))
                return false;
        }
        return true;
    }

    @Getter
    public static class SupAnnotationHandlerData extends AnnotationHandlerData {

        private Method targetMethod;
        private List<Pair<ParameterResolver, Parameter>> parameters = new ArrayList<>();
        private boolean returnContext;
        private String displayName;

        public SupAnnotationHandlerData(Annotation[] annotations, Method method, AbstractWebServer server) {
            super(annotations);
            try {
                displayName = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
                this.targetMethod = method;
                if (Modifier.isAbstract(method.getModifiers()))
                    throw new IllegalHandlerException("Handler might not be abstract" + method.getDeclaringClass().getSimpleName() + "#" + method.getName());
                boolean found;
                for (Parameter parameter : method.getParameters()) {
                    if (parameter.isAnnotationPresent(UseResolver.class)) {
                        try {
                            parameters.add(new Pair<>(parameter.getAnnotation(UseResolver.class).value().newInstance(), parameter));
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    found = false;
                    for (ParameterResolver parameterResolver : costomParameterResolvers) {
                        if (parameterResolver.transformable(parameter)) {
                            parameters.add(new Pair<>(parameterResolver, parameter));
                            found = true;
                            break;
                        }
                    }
                    if (found) continue;
                    for (ParameterResolver parameterResolver : server.getParameterResolvers()) {
                        if (parameterResolver.transformable(parameter)) {
                            parameters.add(new Pair<>(parameterResolver, parameter));
                            found = true;
                            break;
                        }
                    }
                    if (!found) throw new IllegalHandlerException("Could not find transformer for " + parameter.getName() + " in " + displayName);
                }
                Class<?> returnType = method.getReturnType();
                returnContext = ResponseContent.class.isAssignableFrom(returnType);
                if (!returnContext && returnType != boolean.class &&
                        returnType != Boolean.class &&
                        returnType != void.class) {
                    throw new IllegalHandlerException("Invalid method type of handler " + displayName);
                }
            } catch (IllegalHandlerException ex) {
                setLoadingError(ex);
            } catch (Exception ex) {
                setLoadingError(new IllegalHandlerException("Exception while parsing handler", ex));
            }

        }
    }

}