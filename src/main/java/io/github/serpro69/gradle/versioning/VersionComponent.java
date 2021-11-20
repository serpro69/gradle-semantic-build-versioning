package io.github.serpro69.gradle.versioning;

public enum VersionComponent {
    NONE(-1), PRE_RELEASE(3), PATCH(2), MINOR(1), MAJOR(0);

    private int index;

    VersionComponent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
