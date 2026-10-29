package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Makes "no toolkit in the core" executable. */
class UiIndependenceTest {
    private static final Path ROOT = Path.of("src/main/java/edu/itc/salesreport");
    private static final List<String> TOOLKIT_FREE =
            List.of("model", "ingest", "engine", "ui", "ui/mvp");
    private static final List<String> FORBIDDEN =
            List.of("import javafx.", "import java.awt.", "import javax.swing.");

    @Test
    void toolkitFreePackagesImportNoToolkit() throws IOException {
        var violations = new ArrayList<String>();
        for (String pkg : TOOLKIT_FREE) {
            Path dir = ROOT.resolve(pkg);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> files = Files.list(dir)) {   // direct files only
                for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    for (String line : Files.readAllLines(file)) {
                        String trimmed = line.strip();
                        for (String bad : FORBIDDEN) {
                            if (trimmed.startsWith(bad)) {
                                violations.add(file + ": " + trimmed);
                            }
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "Toolkit imports found: " + violations);
    }
}
