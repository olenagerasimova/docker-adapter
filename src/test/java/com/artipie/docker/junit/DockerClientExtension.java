/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.docker.junit;

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.http.DockerSlice;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.vertx.VertxSliceServer;
import com.jcabi.log.Logger;
import io.vertx.reactivex.core.Vertx;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Docker client extension. When it enabled for test class:
 *  - test methods runs only when `docker --version` returns successfully (otherwise
 *    no method executed);
 *  - temporary dir is created when in BeforeAll phase and destroyed in AfterAll.
 *    Docker command output is stored there;
 *  - Vertx Slice Server with Docker slice is started for each test and stopped after execution;
 *  - DockerClient and DockerRepository fields of test class are populated.
 *
 * @since 0.3
 */
@SuppressWarnings({
    "PMD.AvoidCatchingGenericException",
    "PMD.OnlyOneReturn",
    "PMD.AvoidDuplicateLiterals",
    "PMD.AvoidCatchingThrowable"
})
public final class DockerClientExtension implements BeforeEachCallback, AfterEachCallback,
    BeforeAllCallback, AfterAllCallback, ExecutionCondition {
    /**
     * Namespace for class-wide variables.
     */
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(
        DockerClientExtension.class
    );

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        Logger.debug(this, "beforeAll called");
        final Path temp = Files.createTempDirectory("junit-docker-");
        Logger.debug(this, "Created temp dir: %s", temp.toAbsolutePath().toString());
        final DockerClient client = new DockerClient(temp);
        Logger.debug(this, "Created docker client");
        context.getStore(DockerClientExtension.NAMESPACE).put("client", client);
        context.getStore(DockerClientExtension.NAMESPACE).put("vertx", Vertx.vertx());
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        Logger.debug(this, "evaluateExecutionCondition called");
        return this.ensureDockerInstalled();
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        Logger.debug(this, "beforeEach called");
        final Vertx vertx = context.getStore(DockerClientExtension.NAMESPACE)
            .get("vertx", Vertx.class);
        final VertxSliceServer server = new VertxSliceServer(
            vertx,
            new LoggingSlice(new DockerSlice(new AstoDocker(new InMemoryStorage())))
        );
        Logger.debug(this, "Vertx server is created");
        final int port = server.start();
        Logger.debug(this, "Vertx is listening on port %d now", port);
        final DockerClient client = context.getStore(DockerClientExtension.NAMESPACE)
            .get("client", DockerClient.class);
        final DockerRepository repository = () -> String.format("localhost:%s", port);
        this.injectVariables(context, client, repository);
        context.getStore(
            ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod())
        ).put("server", server);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        Logger.debug(this, "afterEach called");
        final VertxSliceServer server = context.getStore(
            ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod())
        ).remove("server", VertxSliceServer.class);
        server.close();
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        Logger.debug(this, "afterAll called");
        final Vertx vertx = context.getStore(DockerClientExtension.NAMESPACE)
            .remove("vertx", Vertx.class);
        vertx.close();
    }

    /**
     * Ensure that docker cli is installed.
     *
     * @return Test enabled property if docker cli is installed, disabled otherwise.
     * @checkstyle IllegalCatchCheck (50 lines)
     * @checkstyle ReturnCountCheck (50 lines)
     */
    private ConditionEvaluationResult ensureDockerInstalled() {
        DockerClient client = null;
        try {
            final Path tmp = Files.createTempDirectory("junit-docker-cond-");
            client = new DockerClient(tmp);
            final String output = client.run("--version");
            if (!output.startsWith("Docker version")) {
                Logger.debug(this, "`docker --version` call return unexpected result: %s", output);
                return ConditionEvaluationResult.disabled("docker not found");
            }
            return ConditionEvaluationResult.enabled("enabled");
        } catch (final Exception ex) {
            Logger.error(this, "Error when call docker: %s", ex.getMessage());
            return ConditionEvaluationResult.disabled("failure when call docker");
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (final Throwable throwable) {
                Logger.error(this, "Error when close docker: %s", throwable.getMessage());
            }
        }
    }

    /**
     * Injects DockerClient and Docker Repository variables in the test instance.
     *
     * @param context JUnit extension context
     * @param client Docker client instance
     * @param repository Docker repository instance
     * @throws Exception When something get wrong
     */
    private void injectVariables(final ExtensionContext context, final DockerClient client,
        final DockerRepository repository) throws Exception {
        final Object instance = context.getRequiredTestInstance();
        for (final Field field : context.getRequiredTestClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(DockerClient.class)) {
                Logger.debug(
                    this, "Found %s field. Try to set DockerClient instance", field.getName()
                );
                this.ensureFieldIsAccessible(field, instance);
                field.set(instance, client);
            }
            if (field.getType().isAssignableFrom(DockerRepository.class)) {
                Logger.debug(
                    this, "Found %s field. Try to set DockerRepository instance", field.getName()
                );
                this.ensureFieldIsAccessible(field, instance);
                field.set(instance, repository);
            }
        }
    }

    /**
     * Try to set field accessible.
     *
     * @param field Class field that need to be accessible
     * @param instance Object instance
     */
    private void ensureFieldIsAccessible(final Field field, final Object instance) {
        if (!field.canAccess(instance)) {
            Logger.debug(this, "%s field is not accessible. Try to change", field.getName());
            field.setAccessible(true);
            Logger.debug(this, "%s field is accessible now", field.getName());
        }
    }
}
