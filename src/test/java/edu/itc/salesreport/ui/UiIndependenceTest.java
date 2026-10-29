package edu.itc.salesreport.ui;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UiIndependenceTest {

    @Test
    void corePackagesMustNotImportToolkits() throws IOException {
        Path srcDir = Path.of("src/main/java/edu/itc/salesreport");
        String[] restrictedPackages = {"model", "ingest", "engine"};

        for (String pkg : restrictedPackages) {
            Path pkgPath = srcDir.resolve(pkg);
            if (!Files.exists(pkgPath)) continue;

            try (Stream<Path> paths = Files.walk(pkgPath)) {
                paths.filter(p -> p.toString().endsWith(".java") && !p.toString().contains("fx/legacy")).forEach(file -> {
                    try {
                        for (String line : Files.readAllLines(file)) {
                            String trimmed = line.strip();
                            if (trimmed.startsWith("import ")) {
                                assertFalse(trimmed.startsWith("import javafx."), file + " imports javafx: " + trimmed);
                                assertFalse(trimmed.startsWith("import java.awt."), file + " imports java.awt: " + trimmed);
                                assertFalse(trimmed.startsWith("import javax.swing."), file + " imports javax.swing: " + trimmed);
                            }
                        }
                    } catch (IOException e) {
                        fail(e);
                    }
                });
            }
        }
    }
}