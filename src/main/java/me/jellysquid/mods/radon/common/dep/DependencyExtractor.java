package me.jellysquid.mods.radon.common.dep;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.Platform;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DependencyExtractor {
    private static final Logger LOGGER = LogManager.getLogger("Radon");

    private static final Path EXTRACT_PATH = Paths.get("./natives/radon");
    private static final String INTERNAL_REPOSITORY_BASE_PATH = "/natives/radon/";
    private static final String LWJGL_PLATFORM_CLASSIFIER = getPlatformClassifier();

    public static void installLwjglNatives(String name, String version) {
        MavenIdentifier identifier = getLwjglMavenArtifact(name, version);
        String internalPath = getInternalArtifactPath(identifier);

        Path installDir = installDependency(internalPath, identifier);

        Configuration.LIBRARY_PATH.set(Configuration.LIBRARY_PATH.get() + File.pathSeparator + installDir);
    }

    private static MavenIdentifier getLwjglMavenArtifact(String name, String version) {
        return new MavenIdentifier("org.lwjgl", name, version, LWJGL_PLATFORM_CLASSIFIER);
    }

    private static String getInternalArtifactPath(MavenIdentifier identifier) {
        return INTERNAL_REPOSITORY_BASE_PATH + identifier.getFileName();
    }

    private static String getPlatformClassifier() {
        switch (Platform.get()) {
            case WINDOWS:
                return "natives-windows";
            case MACOSX:
                return "natives-macos";
            case LINUX:
                return "natives-linux";
            default:
                throw new IllegalStateException("Platform not supported");
        }
    }

    public static Path installDependency(String path, MavenIdentifier identifier) {
        Path dir = EXTRACT_PATH
                .resolve(identifier.group)
                .resolve(identifier.name)
                .resolve(identifier.version)
                .resolve(identifier.classifier)
                .toAbsolutePath();

        if (Files.isDirectory(dir)) {
            LOGGER.info("Skipping extraction of {} as it appears to already be installed", identifier);
        } else {
            installDependency(path, identifier, dir);
        }

        return dir;

    }

    private static void installDependency(String path, MavenIdentifier identifier, Path dir) {
        LOGGER.info("> Extracting dependency {}", identifier);

        Path tempDir = Paths.get(dir + ".tmp");

        try (InputStream fin = getInternalDependency(path)) {
            if (fin == null) {
                throw new FileNotFoundException("Couldn't find internal resource: " + path);
            }

            extractDependency(fin, tempDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract dependency", e);
        }

        LOGGER.info("> Installing dependency {} into local repository", identifier);

        try {
            Files.move(tempDir, dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move directory " + tempDir + " to " + dir, e);
        }
    }

    private static void extractDependency(InputStream in, Path dir) throws IOException {
        byte[] buffer = new byte[8192];

        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                Path extractPath = dir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(extractPath);

                    continue;
                }

                int size;

                try (OutputStream fos = new FileOutputStream(extractPath.toFile());
                     OutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

                    while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, size);
                    }

                    bos.flush();
                }
            }
        }
    }

    private static InputStream getInternalDependency(String path) {
        return DependencyExtractor.class.getResourceAsStream(path);
    }
}
