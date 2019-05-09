package hive.kirby.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PathNode {
  public final String name;
  public final PathNode[] children;

  public PathNode(Path root) {
    name = "root";
    children = getChildren(root);
  }

  private PathNode(String name) {
    this.name = name;
    this.children = null;
  }

  private PathNode(final String name, final PathNode[] children) {
    this.name = name;
    this.children = children;
  }

  private PathNode[] getChildren(Path path) {
    try {
      final var children = new ArrayList<PathNode>();
      Files.list(path).forEach(e -> {
        final var name = e.getFileName().toString();
        if (Files.isDirectory(e)) {
          children.add(new PathNode(name, getChildren(e)));
        } else {
          children.add(new PathNode(name));
        }
      });
      return children.toArray(PathNode[]::new);
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  public static PathNode getEmptyDirInstance(String name) {
    return new PathNode(name, new PathNode[0]);
  }
}
