package io.github.splotycode.mosaik.webapi.handler.anotation;

import io.github.splotycode.mosaik.util.condition.ClassConditions;
import io.github.splotycode.mosaik.util.datafactory.DataFactory;
import io.github.splotycode.mosaik.util.datafactory.DataKey;
import io.github.splotycode.mosaik.util.reflection.annotation.MultiAnnotationContext;
import io.github.splotycode.mosaik.util.reflection.annotation.method.AnnotationHandler;
import io.github.splotycode.mosaik.util.reflection.annotation.parameter.ParameterResolver;
import io.github.splotycode.mosaik.webapi.handler.HttpHandler;
import io.github.splotycode.mosaik.webapi.request.HandleRequestException;
import io.github.splotycode.mosaik.webapi.request.Request;
import io.github.splotycode.mosaik.webapi.response.content.ResponseContent;
import io.github.splotycode.mosaik.webapi.server.WebServer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotationHttpHandler extends MultiAnnotationContext<AnnotationHttpHandler, AnnotationHandlerData> implements HttpHandler {

    public static DataKey<Request> REQUEST = new DataKey<>("web.request");
    public static DataKey<AnnotationHandlerData> GLOBAL = new DataKey<>("web.global");
    public static DataKey<AnnotationHandlerData.SupAnnotationHandlerData> SUP = new DataKey<>("web.sup");

    private Collection<AnnotationHandler<AnnotationHttpHandler, Annotation, AnnotationHandlerData>> costom = new ArrayList<>();

    public void addCostomHandler(AnnotationHandler<AnnotationHttpHandler, Annotation, AnnotationHandlerData> handler) {
        costom.add(handler);
    }

    private WebServer webServer;

    public AnnotationHttpHandler(Class clazz, WebServer webServer) {
        if (webServer == null) throw new NullPointerException("webServer");
        this.webServer = webServer;
        feed(clazz);
    }

    public AnnotationHttpHandler(Object object, WebServer webServer) {
        if (webServer == null) throw new NullPointerException("webServer");
        this.webServer = webServer;
        feedObject(object);
    }

    @Override
    public boolean valid(Request request) {
        return global.valid(request) && sub.stream().anyMatch(sub -> sub.valid(request));
    }

    @Override
    public boolean handle(Request request) throws HandleRequestException {
        if (global.getLoadError() != null) {
            throw new HandleRequestException("Trying to work with crashed Handler: " + clazz.getSimpleName(), global.getLoadError());
        }
        global.applyCashingConfiguration(request.getResponse());

        for (AnnotationHandlerData data : sub.stream().filter(sub -> sub.valid(request)).sorted(Comparator.comparingInt(AnnotationHandlerData::getPriority)).collect(Collectors.toList())) {
            AnnotationHandlerData.SupAnnotationHandlerData sup = (AnnotationHandlerData.SupAnnotationHandlerData) data;

            sup.applyCashingConfiguration(request.getResponse());
            try {
                DataFactory info = new DataFactory();
                info.putData(REQUEST, request);
                info.putData(GLOBAL, global);
                info.putData(SUP, sup);

                Object result = callmethod(sup, info);
                if (sup.isReturnContext()) {
                    request.getResponse().setContent((ResponseContent) result);
                } else if (result != null){
                    boolean cancel = (boolean) result;
                    if (cancel) return true;
                }
            } catch (Throwable ex) {
                throw new HandleRequestException("Could not invoke Method: " + sup.getMethod(), ex);
            }
        }
        return false;
    }

    @Override
    public int priority() {
        return global.getPriority();
    }

    @Override
    protected Collection<ParameterResolver> additionalParameterResolver() {
        return webServer.getParameterResolvers();
    }

    @Override
    protected Class<? extends AnnotationHandlerData> globalDataClass() {
        return AnnotationHandlerData.class;
    }

    @Override
    protected Predicate<AnnotatedElement> methodPredicate() {
        return ClassConditions.needOneAnnotation(AnnotationHandlerFinder.getHandlerAnnotation());
    }

    @Override
    protected Class<? extends AnnotationHandlerData.SupAnnotationHandlerData> methodDataClass() {
        return AnnotationHandlerData.SupAnnotationHandlerData.class;
    }

    @Override
    public AnnotationHttpHandler self() {
        return this;
    }

    @Override
    public Collection<AnnotationHandler<AnnotationHttpHandler, Annotation, AnnotationHandlerData>> getAnnotationHandlers() {
        return costom;
    }
}
