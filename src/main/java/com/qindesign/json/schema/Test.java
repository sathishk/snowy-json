/*
 * Created by shawn on 5/7/20 11:07 PM.
 */
package com.qindesign.json.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Runs the test suite.
 */
public class Test {
  private static final Class<?> CLASS = Main.class;
  private static final Logger logger = Logger.getLogger(CLASS.getName());

  private static final String TEST_SCHEMA = "test-schema.json";

  /**
   * Holds the results of one test.
   */
  private static final class Result {
    int total;
    int passed;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + CLASS.getName() + " <suite location>");
      System.exit(1);
      return;
    }

    File root = new File(args[0]);
    if (!root.isDirectory()) {
      logger.severe("Not a directory: " + args[0]);
      System.exit(1);
    }

    File testSchemaFile = root.toPath().resolve(TEST_SCHEMA).toFile();

    // Load the test schema
    JsonElement testSchema = Main.parse(testSchemaFile);
    logger.info("Loaded test schema");

    File testDir = root.toPath().resolve("tests/draft2019-09").toFile();
    if (!testDir.isDirectory()) {
      logger.severe("Not a directory: " + testDir);
      System.exit(1);
    }

    Map<String, Result> results = new TreeMap<>();
    Result allResult = new Result();
    Instant start = Instant.now();

    // Validate and test as we go
    Files.walkFileTree(testDir.toPath(), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        JsonElement instance = Main.parse(file.toFile());

        // Validate the test
        try {
          if (!Validator.validate(testSchema, instance, file.toUri())) {
            logger.warning("Not a valid test: " + file);
            return FileVisitResult.CONTINUE;
          }
        } catch (MalformedSchemaException ex) {
          logger.log(Level.SEVERE, "Malformed schema: " + file, ex);
          return FileVisitResult.CONTINUE;
        }

        // Run the suite
        Result r = runSuite(file, instance.getAsJsonArray());
        allResult.total += r.total;
        allResult.passed += r.passed;
        results.put(testDir.toPath().relativize(file).toString(), r);

        return FileVisitResult.CONTINUE;
      }
    });

    int maxLen = results.keySet().stream().mapToInt(String::length).max().getAsInt();

    Duration testDur = Duration.between(start, Instant.now());
    System.out.printf("%-" + maxLen + "s  %-4s  %-4s  %-5s\n", "Name", "Pass", "Fail", "Total");
    IntStream.range(0, maxLen + 19).forEach(i -> System.out.print('-'));
    System.out.println();
    results.forEach((name, result) -> {
      System.out.printf("%-" + maxLen + "s  %4d  %4d  %5d\n",
                        name, result.passed, result.total - result.passed, result.total);
    });
    IntStream.range(0, maxLen + 19).forEach(i -> System.out.print('-'));
    System.out.println();
    System.out.printf("Pass:%d Fail:%d Total:%d\n",
                      allResult.passed, allResult.total - allResult.passed, allResult.total);
    System.out.println("Duration: " + testDur);
  }

  private static Result runSuite(Path file, JsonArray suite) {
    Result suiteResult = new Result();

    int groupIndex = 0;
    for (JsonElement g : suite) {
      JsonObject group = g.getAsJsonObject();
      String groupDescription = group.get("description").getAsString();
      JsonElement schema = group.get("schema");

      int testIndex = 0;
      for (JsonElement t : group.getAsJsonArray("tests")) {
        JsonObject test = t.getAsJsonObject();
        String description = test.get("description").getAsString();
        JsonElement data = test.get("data");
        boolean valid = test.get("valid").getAsBoolean();

        URI uri = file.toUri();
        try {
          uri = new URI(uri.getScheme(),
                        uri.getRawSchemeSpecificPart() + "/" + groupIndex + "/" + testIndex,
                        null);
        } catch (URISyntaxException ex) {
          throw new IllegalArgumentException(ex);
        }

        suiteResult.total++;
        logger.info("Testing " + uri);
        try {
          boolean result = Validator.validate(schema, data, uri);
          if (result != valid) {
            logger.info(uri + ": Bad result: got=" + result + " want=" + valid);
          } else {
            suiteResult.passed++;
          }
        } catch (MalformedSchemaException ex) {
          if (valid) {
            logger.info(uri + ": Bad result: got=Malformed schema: " + ex.getMessage());
          }
        }
        testIndex++;
      }

      groupIndex++;
    }

    return suiteResult;
  }
}
