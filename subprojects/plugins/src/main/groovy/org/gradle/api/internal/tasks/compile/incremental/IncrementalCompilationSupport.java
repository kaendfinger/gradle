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

package org.gradle.api.internal.tasks.compile.incremental;

import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.tasks.compile.CleaningJavaCompiler;
import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.internal.tasks.compile.incremental.graph.ClassDependencyInfoExtractor;
import org.gradle.api.internal.tasks.compile.incremental.graph.ClassDependencyInfoSerializer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

public class IncrementalCompilationSupport {

    private static final Logger LOG = Logging.getLogger(IncrementalCompilationSupport.class);
    private final JarSnapshotFeeder jarSnapshotFeeder;
    private final ClassDependencyInfoSerializer dependencyInfoSerializer;
    private final FileOperations fileOperations;
    private final ClassDependencyInfoExtractor extractor;
    private final CleaningJavaCompiler cleaningCompiler;
    private final String displayName;
    private final StaleClassesDetecter staleClassDetecter;

    public IncrementalCompilationSupport(JarSnapshotFeeder jarSnapshotFeeder, ClassDependencyInfoSerializer dependencyInfoSerializer,
                                         FileOperations fileOperations, ClassDependencyInfoExtractor extractor,
                                         CleaningJavaCompiler cleaningCompiler, String displayName, StaleClassesDetecter staleClassDetecter) {
        this.jarSnapshotFeeder = jarSnapshotFeeder;
        this.dependencyInfoSerializer = dependencyInfoSerializer;
        this.fileOperations = fileOperations;
        this.extractor = extractor;
        this.cleaningCompiler = cleaningCompiler;
        this.displayName = displayName;
        this.staleClassDetecter = staleClassDetecter;
    }

    public Compiler<JavaCompileSpec> prepareCompiler(final IncrementalTaskInputs inputs, final CompilationSourceDirs sourceDirs) {
        final Compiler<JavaCompileSpec> compiler = getCompiler(inputs, sourceDirs);
        return new IncrementalCompilationFinalizer(compiler, extractor, dependencyInfoSerializer, jarSnapshotFeeder, new ClasspathJarFinder(fileOperations));
    }

    private Compiler<JavaCompileSpec> getCompiler(IncrementalTaskInputs inputs, CompilationSourceDirs sourceDirs) {
        if (!inputs.isIncremental()) {
            LOG.lifecycle("{} - is not incremental (e.g. outputs have changed, no previous execution, etc)", displayName);
            return cleaningCompiler;
        }
        if (!sourceDirs.areSourceDirsKnown()) {
            LOG.lifecycle("{} - is not incremental. Unable to infer the source directories.", displayName);
            return cleaningCompiler;
        }
        if (!dependencyInfoSerializer.isInfoAvailable()) {
            LOG.lifecycle("{} - is not incremental. No class dependency data available from previous build.", displayName);
            return cleaningCompiler;
        }
        return new SelectiveCompiler(inputs, cleaningCompiler, staleClassDetecter, new IncrementalCompilationInitializer(fileOperations));
    }
}
