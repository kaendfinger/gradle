/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling;

import org.gradle.api.Incubating;

/**
 * Used to execute a {@link BuildAction} in the build process.
 *
 * @param <T> The type of result produced by this executer.
 * @since 1.8
 */
@Incubating
public interface BuildActionExecuter<T> extends LongRunningOperation {
    /**
     * Runs the action, blocking until its result is available.
     *
     * @since 1.8
     */
    T run() throws GradleConnectionException;

    /**
     * Starts executing the action, passing the result to the given handler when complete. This method returns immediately, and the result is later passed to the given handler's {@link
     * ResultHandler#onComplete(Object)} method. If the operation fails, the handler's {@link ResultHandler#onFailure(GradleConnectionException)} method is called with the appropriate exception. See
     * {@link #run()} for a description of the various exceptions that the operation may fail with.
     *
     * @param handler The handler to supply the result to.
     * @since 1.8
     */
    void run(ResultHandler<? super T> handler) throws IllegalStateException;
}