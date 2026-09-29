package routeplanner.bridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BridgeIsolationTest {

    private static final Path BRIDGE_SOURCES = Path.of("src", "main", "java", "routeplanner", "bridge");

    @Test
    @DisplayName("The abstraction side never references adapter, legacy or concrete provider packages")
    void abstractionDependsOnlyOnTheImplementorInterface() throws IOException {
        List<Path> files;
        try (Stream<Path> stream = Files.list(BRIDGE_SOURCES)) {
            files = stream.filter(path -> path.toString().endsWith(".java")).toList();
        }
        assertFalse(files.isEmpty());
        for (Path file : files) {
            String source = Files.readString(file);
            assertFalse(source.contains("routeplanner.legacy"), file + " references the legacy package");
            assertFalse(source.contains("routeplanner.adapter"), file + " references the adapter package");
            assertFalse(source.contains("routeplanner.provider"), file + " references a concrete provider");
            assertTrue(source.contains("package routeplanner.bridge;"));
        }
    }
}
