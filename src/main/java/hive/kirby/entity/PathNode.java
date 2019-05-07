package hive.kirby.entity;

public class PathNode {
  public final String name;
  public final PathNode[] children;

  public PathNode(String name) {
    this.name = name;
    this.children = null;
  }

  public PathNode(final String name, final PathNode[] children) {
    this.name = name;
    this.children = children;
  }
}
