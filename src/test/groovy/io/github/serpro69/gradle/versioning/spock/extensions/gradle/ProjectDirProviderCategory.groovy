package io.github.serpro69.gradle.versioning.spock.extensions.gradle

import io.github.serpro69.gradle.versioning.TestRepository
import org.junit.rules.TemporaryFolder

import java.nio.file.Path

class ProjectDirProviderCategory {
    static get(File file) { file }

    static get(Path path) { path.toFile() }

    static get(TestRepository testRepository) { testRepository.repository.workTree }

    static get(TemporaryFolder temporaryFolder) { temporaryFolder.root }
}
