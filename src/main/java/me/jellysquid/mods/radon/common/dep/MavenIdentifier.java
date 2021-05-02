package me.jellysquid.mods.radon.common.dep;

public class MavenIdentifier {
    public final String group, name, version, classifier;

    public MavenIdentifier(String group, String name, String version, String classifier) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.classifier = classifier;
    }

    public String getFileName() {
        return String.format("%s-%s-%s.jar", this.name, this.version, this.classifier);
    }

    @Override
    public String toString() {
        return String.format("MavenIdentifier{group='%s', name='%s', version='%s', classifier='%s'}",
                this.group, this.name, this.version, this.classifier);
    }
}
