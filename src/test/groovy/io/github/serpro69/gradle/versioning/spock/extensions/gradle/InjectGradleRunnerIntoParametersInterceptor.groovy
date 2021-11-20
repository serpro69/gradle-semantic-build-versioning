package io.github.serpro69.gradle.versioning.spock.extensions.gradle

import org.gradle.testkit.runner.GradleRunner
import org.spockframework.runtime.extension.IMethodInvocation

import static java.nio.file.Files.createTempDirectory

class InjectGradleRunnerIntoParametersInterceptor extends InjectGradleRunnerInterceptorBase {
    @Override
    public void intercept(IMethodInvocation invocation) {
        // create a map of all GradleRunner parameters with their parameter index
        Map<Class<? extends Object>, Integer> parameters = [:]
        invocation.method.reflection.parameterTypes.eachWithIndex { parameter, i -> parameters << [(parameter): i] }
        parameters = parameters.findAll { it.key == GradleRunner }

        // enlarge arguments array if necessary
        def lastGradleRunnerParameterIndex = parameters*.value.max()
        lastGradleRunnerParameterIndex = lastGradleRunnerParameterIndex == null ? 0 : lastGradleRunnerParameterIndex + 1
        if(invocation.arguments.length < lastGradleRunnerParameterIndex) {
            def newArguments = new Object[lastGradleRunnerParameterIndex]
            System.arraycopy invocation.arguments, 0, newArguments, 0, invocation.arguments.length
            invocation.arguments = newArguments
        }

        // find all parameters to fill
        def parametersToFill = parameters.findAll { !invocation.arguments[it.value] }

        if(!parametersToFill) {
            invocation.proceed()
            return
        }

        def parameterAnnotations = invocation.method.reflection.parameterAnnotations

        List<File> temporaryProjectDirs = []
        try {
            parametersToFill.each { parameter, i ->
                // determine the project dir to use
                def projectDirClosure = parameterAnnotations[i].find { it instanceof ProjectDirProvider }?.value()

                File projectDir

                if(!projectDirClosure) {
                    projectDir = createTempDirectory('gradleRunner_').toFile()
                    temporaryProjectDirs << projectDir
                } else {
                    projectDir = determineProjectDir(projectDirClosure.newInstance(invocation.instance, invocation.instance)(), "parameter '${invocation.feature.parameterNames[i]}'")
                }

                invocation.arguments[i] = prepareProjectDir(projectDir)
            }

            invocation.proceed()
        } finally {
            temporaryProjectDirs*.deleteDir()
        }
    }
}
