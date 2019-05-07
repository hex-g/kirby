package hive.kirby.util;

import hive.kirby.entity.PathNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public final class Utils {
  private Utils() {
  }

  public static PathNode[] getTree(final Path path) {
    try {
      final var children = new ArrayList<PathNode>();
      Files.list(path).forEach(e -> {
        final var name = e.getFileName().toString();
        if (Files.isDirectory(e)) {
          children.add(new PathNode(name, getTree(e)));
        } else {
          children.add(new PathNode(name));
        }
      });
      return children.toArray(PathNode[]::new);
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }
}
