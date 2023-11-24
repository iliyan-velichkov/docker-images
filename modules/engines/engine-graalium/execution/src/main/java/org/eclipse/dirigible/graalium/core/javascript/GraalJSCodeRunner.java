/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible
 * contributors SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.graalium.core.javascript;

import org.eclipse.dirigible.graalium.core.CodeRunner;
import org.eclipse.dirigible.graalium.core.graal.ContextCreator;
import org.eclipse.dirigible.graalium.core.graal.EngineCreator;
import org.eclipse.dirigible.graalium.core.graal.globals.GlobalFunction;
import org.eclipse.dirigible.graalium.core.graal.globals.GlobalObject;
import org.eclipse.dirigible.graalium.core.javascript.modules.ModuleResolver;
import org.eclipse.dirigible.graalium.core.javascript.modules.ModuleType;
import org.eclipse.dirigible.graalium.core.javascript.modules.downloadable.DownloadableModuleResolver;
import org.eclipse.dirigible.graalium.core.javascript.polyfills.JavascriptPolyfill;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The Class GraalJSCodeRunner.
 */
public class GraalJSCodeRunner implements CodeRunner<Source, Value> {

    /**
     * The current working directory path.
     */
    private final Path currentWorkingDirectoryPath;

    /**
     * The graal context.
     */
    private final Context graalContext;

    /**
     * The graal JS source creator.
     */
    private final GraalJSSourceCreator graalJSSourceCreator;

    /**
     * The graal JS interceptor.
     */
    private final GraalJSInterceptor graalJSInterceptor;

    /**
     * Instantiates a new graal JS code runner.
     *
     * @param builder the builder
     */
    private GraalJSCodeRunner(Builder builder) {
        this.currentWorkingDirectoryPath = builder.workingDirectoryPath;

        Consumer<Context.Builder> onBeforeContextCreatedHook = provideOnBeforeContextCreatedHook(builder.onBeforeContextCreatedListeners);
        Consumer<Context> onAfterContextCreatedHook = provideOnAfterContextCreatedHook(builder.onAfterContextCreatedListener);

        Engine graalEngine = builder.waitForDebugger ? EngineCreator.getOrCreateDebuggableEngine() : EngineCreator.getOrCreateEngine();
        DownloadableModuleResolver downloadableModuleResolver = new DownloadableModuleResolver(builder.dependenciesCachePath);

        GraalJSFileSystem graalJSFileSystem = new GraalJSFileSystem(currentWorkingDirectoryPath, builder.moduleResolvers,
                downloadableModuleResolver, builder.onRealPathNotFound, builder.delegateFileSystem);

        graalJSSourceCreator = new GraalJSSourceCreator(builder.jsModuleType);
        graalJSInterceptor = builder.interceptor;
        graalContext = new ContextCreator(graalEngine, currentWorkingDirectoryPath, currentWorkingDirectoryPath, null,
                onBeforeContextCreatedHook, onAfterContextCreatedHook, graalJSFileSystem).createContext();

        addGlobalObjects(builder.globalObjects);
        registerPolyfills(graalContext, builder.jsPolyfills);
    }

    /**
     * Gets the graal context.
     *
     * @return the graal context
     */
    public Context getGraalContext() {
        return graalContext;
    }

    /**
     * Provide on before context created hook.
     *
     * @param onBeforeContextCreatedListeners the on before context created listeners
     * @return the consumer
     */
    private static Consumer<Context.Builder> provideOnBeforeContextCreatedHook(
            List<Consumer<Context.Builder>> onBeforeContextCreatedListeners) {
        return contextBuilder -> onBeforeContextCreatedListeners.forEach(x -> x.accept(contextBuilder));
    }

    /**
     * Provide on after context created hook.
     *
     * @param onAfterContextCreatedListeners the on after context created listeners
     * @return the consumer
     */
    private static Consumer<Context> provideOnAfterContextCreatedHook(List<Consumer<Context>> onAfterContextCreatedListeners) {
        return context -> onAfterContextCreatedListeners.forEach(x -> x.accept(context));
    }

    /**
     * Register polyfills.
     *
     * @param context the context
     * @param jsPolyfills the js polyfills
     */
    private void registerPolyfills(Context context, List<JavascriptPolyfill> jsPolyfills) {
        jsPolyfills.stream()
                   .map(polyfill -> graalJSSourceCreator.createInternalSource(polyfill.getSource(), polyfill.getFileName()))
                   .forEach(context::eval);
    }

    /**
     * Run.
     *
     * @param codeFilePath the code file path
     * @return the source
     */
    @Override
    public Source prepareSource(Path codeFilePath) {
        Path relativeCodeFilePath = currentWorkingDirectoryPath.resolve(codeFilePath);
        return graalJSSourceCreator.createSource(relativeCodeFilePath);
    }

    /**
     * Run.
     *
     * @param codeSource the code source
     * @return the value
     */
    @Override
    public Value run(Source codeSource) {
        Value result = graalContext.eval(codeSource);
        rethrowIfError(result);
        return result;
    }

    /**
     * Gets the graal JS interceptor.
     *
     * @return the graal JS interceptor
     */
    public GraalJSInterceptor getGraalJSInterceptor() {
        return graalJSInterceptor;
    }

    /**
     * Gets the current working directory path.
     *
     * @return the current working directory path
     */
    public Path getCurrentWorkingDirectoryPath() {
        return currentWorkingDirectoryPath;
    }

    /**
     * Adds the global objects.
     *
     * @param globalObjects the js global objects
     */
    public void addGlobalObjects(List<GlobalObject> globalObjects) {
        Value contextBindings = graalContext.getBindings("js");
        globalObjects.forEach(global -> contextBindings.putMember(global.getName(), global.getValue()));
    }

    /**
     * Adds the global functions.
     *
     * @param globalFunctions the js global functions
     */
    public void addGlobalFunctions(List<GlobalFunction> globalFunctions) {
        Value contextBindings = graalContext.getBindings("js");
        globalFunctions.forEach(global -> contextBindings.putMember(global.getName(), global));
    }

    /**
     * Parses the.
     *
     * @param codeFilePath the code file path
     * @return the value
     */
    public Value parse(Path codeFilePath) {
        Path relativeCodeFilePath = currentWorkingDirectoryPath.resolve(codeFilePath);
        Source codeSource = graalJSSourceCreator.createSource(relativeCodeFilePath);
        return parse(codeSource);
    }

    /**
     * Parses the.
     *
     * @param codeSource the code source
     * @return the value
     */
    public Value parse(Source codeSource) {
        return graalContext.parse(codeSource);
    }

    /**
     * Leave.
     */
    public void leave() {
        graalContext.leave();
    }

    /**
     * Rethrow if error.
     *
     * @param maybeError the maybe error
     */
    private static void rethrowIfError(Value maybeError) {
        if (maybeError.isException()) {
            throw maybeError.throwException();
        }
    }

    /**
     * New builder.
     *
     * @param currentWorkingDirectoryPath the current working directory path
     * @param cachesPath the caches path
     * @return the builder
     */
    public static Builder newBuilder(Path currentWorkingDirectoryPath, Path cachesPath) {
        return new Builder(currentWorkingDirectoryPath, cachesPath);
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        if (graalContext != null) {
            graalContext.close(false);
        }
    }

    /**
     * The Class Builder.
     */
    public static class Builder {

        /**
         * The working directory path.
         */
        private final Path workingDirectoryPath;

        /**
         * The dependencies cache path.
         */
        private final Path dependenciesCachePath;

        /**
         * The wait for debugger.
         */
        private boolean waitForDebugger = false;

        /**
         * The js module type.
         */
        private ModuleType jsModuleType = ModuleType.BASED_ON_FILE_EXTENSION;

        /**
         * The js polyfills.
         */
        private final List<JavascriptPolyfill> jsPolyfills = new ArrayList<>();

        /**
         * The global objects.
         */
        private final List<GlobalObject> globalObjects = new ArrayList<>();

        /**
         * The on before context created listeners.
         */
        private final List<Consumer<Context.Builder>> onBeforeContextCreatedListeners = new ArrayList<>();

        /**
         * The on after context created listener.
         */
        private final List<Consumer<Context>> onAfterContextCreatedListener = new ArrayList<>();

        /**
         * The module resolvers.
         */
        private final List<ModuleResolver> moduleResolvers = new ArrayList<>();

        /**
         * The interceptor *.
         */
        private GraalJSInterceptor interceptor;

        /**
         * The callback to invoke in GraalJS' FileSystem when a Path's toRealPath fails.
         */
        private Function<Path, Path> onRealPathNotFound;

        /**
         * The file system to delegate to in GraalJS' file system
         */
        private FileSystem delegateFileSystem = FileSystems.getDefault();

        /**
         * Instantiates a new builder.
         *
         * @param workingDirectoryPath the working directory path
         * @param cachesPath the caches path
         */
        public Builder(Path workingDirectoryPath, Path cachesPath) {
            this.workingDirectoryPath = workingDirectoryPath;
            this.dependenciesCachePath = cachesPath.resolve("dependencies-cache");
        }

        /**
         * With JS module type.
         *
         * @param jsModuleType the js module type
         * @return the builder
         */
        public Builder withJSModuleType(ModuleType jsModuleType) {
            this.jsModuleType = jsModuleType;
            return this;
        }

        /**
         * Wait for debugger.
         *
         * @param shouldWaitForDebugger the should wait for debugger
         * @return the builder
         */
        public Builder waitForDebugger(boolean shouldWaitForDebugger) {
            waitForDebugger = shouldWaitForDebugger;
            return this;
        }

        /**
         * Wait for debugger.
         *
         * @param shouldWaitForDebugger the should wait for debugger
         * @return the builder
         */
        public Builder waitForDebugger(Supplier<Boolean> shouldWaitForDebugger) {
            waitForDebugger = shouldWaitForDebugger.get();
            return this;
        }

        /**
         * Adds the JS polyfill.
         *
         * @param jsPolyfill the js polyfill
         * @return the builder
         */
        public Builder addJSPolyfill(JavascriptPolyfill jsPolyfill) {
            jsPolyfills.add(jsPolyfill);
            return this;
        }

        /**
         * Adds the global object.
         *
         * @param globalObject the js global object
         * @return the builder
         */
        public Builder addGlobalObject(GlobalObject globalObject) {
            globalObjects.add(globalObject);
            return this;
        }

        /**
         * Adds the module resolver.
         *
         * @param moduleResolver the module resolver
         * @return the builder
         */
        public Builder addModuleResolver(ModuleResolver moduleResolver) {
            moduleResolvers.add(moduleResolver);
            return this;
        }

        /**
         * Adds the on before context created listener.
         *
         * @param onBeforeContextCreatedListener the on before context created listener
         * @return the builder
         */
        public Builder addOnBeforeContextCreatedListener(Consumer<Context.Builder> onBeforeContextCreatedListener) {
            if (onBeforeContextCreatedListener != null) {
                onBeforeContextCreatedListeners.add(onBeforeContextCreatedListener);
            }
            return this;
        }

        /**
         * Adds the on after context created listener.
         *
         * @param onAfterContextCreatedListener the on after context created listener
         * @return the builder
         */
        public Builder addOnAfterContextCreatedListener(Consumer<Context> onAfterContextCreatedListener) {
            if (onAfterContextCreatedListener != null) {
                this.onAfterContextCreatedListener.add(onAfterContextCreatedListener);
            }
            return this;
        }

        /**
         * Sets a callback to invoke when a Path's toRealPath fails in GraalJS' FileSystem. This callback
         * should return another Path if it could be constructed or throw an exception
         *
         * @param onRealPathNotFound the callback to invoke
         * @return the builder
         */
        public Builder setOnRealPathNotFound(Function<Path, Path> onRealPathNotFound) {
            this.onRealPathNotFound = onRealPathNotFound;
            return this;
        }

        /**
         * Sets a file system to use in GraalJS' FileSystem. Defaults to the system one.
         *
         * @param fileSystem the file system to use
         * @return
         */
        public Builder setDelegateFileSystem(FileSystem fileSystem) {
            this.delegateFileSystem = fileSystem;
            return this;
        }

        /**
         * Adds the on after context created listener.
         *
         * @param interceptor the interceptor
         * @return the builder
         */
        public Builder setInterceptor(GraalJSInterceptor interceptor) {
            if (interceptor != null) {
                this.interceptor = interceptor;
            }
            return this;
        }

        /**
         * Builds the.
         *
         * @return the graal JS code runner
         * @throws IllegalStateException the illegal state exception
         */
        public GraalJSCodeRunner build() throws IllegalStateException {
            if (workingDirectoryPath == null || dependenciesCachePath == null) {
                throw new RuntimeException("Please, provide all folder paths!");
            }

            return new GraalJSCodeRunner(this);
        }

    }
}
