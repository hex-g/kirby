package hive.kirby.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hive.kirby.entity.Note;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import static hive.pandora.constant.HiveInternalHeaders.AUTHENTICATED_USER_ID;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NoteControllerTest {
  @Value("${hive.kirby.storage-directory}")
  private String rootDir;
  private String userId;
  private MockMvc mockMvc;

  @Before
  public void setup() {
    userId = Integer.toString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));

    final var noteController = new NoteController();

    ReflectionTestUtils.setField(noteController, "rootDir", rootDir);

    mockMvc = MockMvcBuilders.standaloneSetup(noteController).build();
  }

  @Test
  public void givenPathContainsParentDirectoryReference_whenNoteIsRetrieved_then400IsReceived() throws Exception {
    mockMvc
        .perform(
            get("/note")
                .header(AUTHENTICATED_USER_ID, userId)
                .param("path", "..")
        )
        .andExpect(status().isBadRequest())
        .andExpect(status().reason("Invalid path"));
  }

  @Test
  public void givenPathIsValidAndNoteDoesNotExist_whenNoteIsRetrieved_then404IsReceived() throws Exception {
    mockMvc
        .perform(
            get("/note")
                .header(AUTHENTICATED_USER_ID, userId)
                .param("path", "a/valid/path/to/a/file")
        )
        .andExpect(status().isNotFound())
        .andExpect(status().reason("Note not found"));
  }

  @Test
  public void givenPathIsValidAndNoteExists_whenNoteIsRetrieved_then200IsReceived() throws Exception {
    final var id = userId;
    final var dirs = rootDir + "/" + id + "/a/valid/path/to/a";
    final var fullPath = dirs + "/file";
    final var path = "a/valid/path/to/a/file";

    try {
      Files.createDirectories(Paths.get(dirs));
      Files.createFile(Paths.get(fullPath));

      mockMvc
          .perform(
              get("/note")
                  .header(AUTHENTICATED_USER_ID, userId)
                  .param("path", path)
          )
          .andExpect(status().isOk());
    } finally {
      deleteCreatedFiles();
    }
  }

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

  @Test
  public void givenPathContainsParentDirectoryReference_whenNoteIsSaved_then400IsReceived() throws Exception {
    mockMvc
        .perform(
            post("/note")
                .header(AUTHENTICATED_USER_ID, userId)
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(
                    new Note("..", RandomStringUtils.randomAscii(2048))
                ))
        )
        .andExpect(status().isBadRequest())
        .andExpect(status().reason("Invalid path"));
  }

  @Test
  public void givenPathIsValid_whenNoteIsSaved_then200IsReceived() throws Exception {
    try {
      mockMvc
          .perform(
              post("/note")
                  .header(AUTHENTICATED_USER_ID, userId)
                  .contentType("application/json")
                  .content(new ObjectMapper().writeValueAsString(
                      new Note("a/valid/path", RandomStringUtils.randomAscii(2048))
                  ))
          )
          .andExpect(status().isOk());
    } finally {
      deleteCreatedFiles();
    }
  }

  @Test
  public void givenPathIsValidAndNoteExists_whenNoteIsRetrieved_thenRetrievedResourceIsCorrect() throws Exception {
    final var dirs = rootDir + "/" + userId + "/a/valid/path/to/a";
    final var fullPath = dirs + "/file";
    final var path = "a/valid/path/to/a/file";
    final var fileContent = RandomStringUtils.randomAscii(2048);

    try {
      Files.createDirectories(Paths.get(dirs));
      Files.createFile(Paths.get(fullPath));
      try (var fileOutputStream = new FileOutputStream(fullPath)) {
        fileOutputStream.write(fileContent.getBytes());
      } catch (IOException e) {
        System.err.println("Could not write to created file. File: " + fullPath);
      }

      final var result = mockMvc
          .perform(
              get("/note")
                  .header(AUTHENTICATED_USER_ID, userId)
                  .param("path", path)
                  .contentType(MediaType.APPLICATION_JSON))
          .andReturn();

      final var actual =
          new ObjectMapper().readValue(result.getResponse().getContentAsString(), Note.class);

      assertEquals(path, actual.getPath());
      assertEquals(fileContent, actual.getContent());
    } finally {
      deleteCreatedFiles();
    }
  }

  @Test
  public void givenPathIsValidAndFileDoesNotExist_whenNoteIsSaved_thenFileIsCreatedWithNoteContent() throws Exception {
    final var dirs = rootDir + "/" + userId + "/a/valid/path/to/a";
    final var fullPath = dirs + "/file";
    final var path = "a/valid/path/to/a/file";
    final var fileContent = RandomStringUtils.randomAscii(2048);

    try {
      mockMvc
          .perform(
              post("/note")
                  .header(AUTHENTICATED_USER_ID, userId)
                  .contentType("application/json")
                  .content(new ObjectMapper().writeValueAsString(
                      new Note(path, fileContent)
                  ))
          );

      final var p = Paths.get(fullPath);
      assertTrue(Files.exists(p));

      assertEquals(fileContent, new String(Files.readAllBytes(p)));
    } finally {
      deleteCreatedFiles();
    }
  }

  @Test
  public void givenPathContainsParentDirectoryReference_whenNoteIsDelete_then400IsReceived() throws Exception {
    mockMvc
        .perform(
            delete("/note")
                .header(AUTHENTICATED_USER_ID, userId)
                .param("path", "..")
        )
        .andExpect(status().isBadRequest())
        .andExpect(status().reason("Invalid path"));
  }

  @Test
  public void givenPathIsValidAndNoteExists_whenNoteIsDeleted_then200IsReceived() throws Exception {
    mockMvc
        .perform(
            delete("/note")
                .header(AUTHENTICATED_USER_ID, userId)
                .param("path", "a/valid/path/to/a/file")
        )
        .andExpect(status().isOk());
  }

  @Test
  public void givenPathIsValidAndFileExists_whenNoteIsSaved_thenContentIsUpdated() throws Exception {
    final var path = "a/valid/path/to/a/file";
    final var fileContent = RandomStringUtils.randomAscii(2048);

    try {
      final var p = Paths.get(rootDir, userId, path);
      Files.createDirectories(p.getParent());
      Files.write(p, RandomStringUtils.randomAscii(2048).getBytes());

      mockMvc
          .perform(
              post("/note")
                  .header(AUTHENTICATED_USER_ID, userId)
                  .contentType("application/json")
                  .content(new ObjectMapper().writeValueAsString(
                      new Note(path, fileContent)
                  ))
          );

      assertEquals(fileContent, new String(Files.readAllBytes(p)));
    } finally {
      deleteCreatedFiles();
    }
  }

  @Test
  public void givenPathIsValidAndNoteExists_whenNoteIsDeleted_thenFileAndEmptyParentDirectoriesAreDeleted() throws Exception {
    final var path = "a/valid/path/to/a/file";
    final var p = Paths.get(rootDir, userId, path);
    final var extra = Paths.get(rootDir, userId, "extra");

    try {
      Files.createDirectories(p.getParent());
      Files.createFile(p);
      Files.createFile(extra);

      mockMvc
          .perform(
              delete("/note")
                  .header(AUTHENTICATED_USER_ID, userId)
                  .param("path", "a/valid/path/to/a/file")
          )
          .andExpect(status().isOk());

      assertFalse(Files.exists(p.getName(2)));
      assertTrue(Files.exists(extra));
    } finally {
      deleteCreatedFiles();
    }
  }
}
