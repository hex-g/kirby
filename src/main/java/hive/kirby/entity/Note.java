package hive.kirby.entity;

public class Note {
  private String path;
  private String content;

  public Note() {
  }

  public Note(String path, String content) {
    this.path = path;
    this.content = content;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public String getContent() {
    return content;
  }

  public void setContent(final String content) {
    this.content = content;
  }
}
