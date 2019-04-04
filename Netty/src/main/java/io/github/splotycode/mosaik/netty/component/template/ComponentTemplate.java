package io.github.splotycode.mosaik.netty.component.template;

import io.github.splotycode.mosaik.netty.component.ChannelSystem;
import io.github.splotycode.mosaik.netty.component.INetworkComponent;
import io.github.splotycode.mosaik.netty.component.NetworkComponent;
import io.github.splotycode.mosaik.netty.component.SSLMode;
import io.github.splotycode.mosaik.netty.component.listener.BindListener;
import io.github.splotycode.mosaik.netty.component.listener.BoundListener;
import io.github.splotycode.mosaik.netty.component.listener.UnBoundListener;
import io.github.splotycode.mosaik.util.listener.Listener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ComponentTemplate<S extends ComponentTemplate<S, I>, I extends NetworkComponent<?, ?, I>> implements INetworkComponent<S> {

    protected List<Consumer<I>> tasks = new ArrayList<>();

    public abstract I createComponent();

    protected void apply(I component) {
        tasks.forEach(consumer -> consumer.accept(component));
    }

    @Override
    public S self() {
        return (S) this;
    }

    @Override
    public S port(Supplier<Integer> port) {
        tasks.add(i -> i.port(port));
        return self();
    }

    @Override
    public <O> S option(ChannelOption<O> option, O value) {
        tasks.add(i -> i.option(option, value));
        return self();
    }

    @Override
    public S option(Map<ChannelOption, Object> options) {
        tasks.add(i -> i.option(options));
        return self();
    }

    @Override
    public S onBind(BindListener listener) {
        tasks.add(i -> i.onBind(listener));
        return self();
    }

    @Override
    public S onBound(BoundListener listener) {
        tasks.add(i -> i.onBound(listener));
        return self();
    }

    @Override
    public S onUnBound(UnBoundListener listener) {
        tasks.add(i -> i.onUnBound(listener));
        return self();
    }

    @Override
    public S addListener(Listener listener) {
        tasks.add(i -> i.addListener(listener));
        return self();
    }

    @Override
    public S removeListener(Listener listener) {
        tasks.add(i -> i.removeListener(listener));
        return self();
    }

    @Override
    public S handler(String name, ChannelHandler handler) {
        tasks.add(i -> i.handler(name, handler));
        return self();
    }

    @Override
    public S channelSystem(ChannelSystem channelSystem) {
        tasks.add(i -> i.channelSystem(channelSystem));
        return self();
    }

    @Override
    public S applyDefaults(boolean apply) {
        tasks.add(i -> i.applyDefaults(apply));
        return self();
    }

    @Override
    public S noSSL() {
        tasks.add(NetworkComponent::noSSL);
        return self();
    }

    @Override
    public S ssl(SSLMode sslMode) {
        tasks.add(i -> i.ssl(sslMode));
        return self();
    }

    @Override
    public S logging(boolean logging) {
        tasks.add(i -> i.logging(logging));
        return self();
    }

    @Override
    public S logging(String category) {
        tasks.add(i -> i.logging(category));
        return self();
    }

    @Override
    public S logging(String category, LogLevel logLevel) {
        tasks.add(i -> i.logging(category, logLevel));
        return self();
    }

    @Override
    public S noLogging() {
        tasks.add(NetworkComponent::noLogging);
        return self();
    }

    @Override
    public S nThreads(int nThreads) {
        tasks.add(i -> i.nThreads(nThreads));
        return self();
    }
}