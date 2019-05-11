package hive.kirby.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import static hive.pandora.constant.HiveInternalHeaders.AUTHENTICATED_USER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HomeControllerTest {
  @Value("${hive.kirby.storage-directory}")
  private String rootDir;
  private String userId;
  private MockMvc mockMvc;

  @Before
  public void setup() {
    userId = Integer.toString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));

    final var homeController = new HomeController();

    ReflectionTestUtils.setField(homeController, "rootDir", rootDir);

    mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
  }

  @Test
  public void givenUserDirectoryExists_whenTreeIsRetrieved_thenRetrievedResourceIsCorrect() throws Exception {
    final var path = Paths.get(rootDir, userId, "test", "path");
    final var tree =
        "{\"name\":\"root\",\"children\":[{\"name\":\"test\",\"children\":[{\"name\":\"path\","
            + "\"children\":null}]}]}";

    try {
      Files.createDirectories(path.getParent());
      Files.createFile(path);

      mockMvc
          .perform(
              get("/tree")
                  .header(AUTHENTICATED_USER_ID, userId)
          )
          .andExpect(content().string(tree));
    } finally {
      deleteCreatedFiles();
    }
  }

  @Test
  public void givenUserDirectoryDoesNotExist_whenTreeIsRetrieved_thenRetrievedResourceIsCorrect() throws Exception {
    final var tree = "{\"name\":\"root\",\"children\":[]}";

    mockMvc
        .perform(
            get("/tree")
                .header(AUTHENTICATED_USER_ID, userId)
        )
        .andExpect(content().string(tree));
  }

  // TODO Fix test cleanup
  private void deleteCreatedFiles() {
    final var p = Paths.get(rootDir, userId);

    try {
      if (Files.exists(p)) {
        //noinspection ResultOfMethodCallIgnored
        Files.walk(p)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
      }
    } catch (IOException e) {
      System.err.println(
          "Created files could not be deleted. Root directory name: " + userId + ".");
    }
  }
}
