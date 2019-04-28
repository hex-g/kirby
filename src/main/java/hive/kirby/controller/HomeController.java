package hive.kirby.controller;

import hive.kirby.entity.PathNode;
import hive.kirby.util.DirectoryTreeBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;

import static hive.pandora.constant.HiveInternalHeaders.AUTHENTICATED_USER_ID;

@RestController
@RequestMapping("/")
public class HomeController {
  @Value("${hive.kirby.storage-directory}")
  private String rootDir;

  @GetMapping("/tree")
  public PathNode userDirectoryTree(
      @RequestHeader(name = AUTHENTICATED_USER_ID) final String userId
  ) {
    return new DirectoryTreeBuilder(Paths.get(rootDir, userId).toString()).getRoot();
  }
}
