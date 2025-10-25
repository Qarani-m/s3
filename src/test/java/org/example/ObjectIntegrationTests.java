package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.config.ClientConfig;

import org.example.dto.files.CopyFileInput;
import org.example.dto.files.FileInfo;

import org.example.dto.files.MoveFileInput;
import org.example.dto.files.UpdateFileMetadataInput;
import org.example.http.HttpClient;
import org.example.services.bucket.ObjectClient;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObjectIntegrationTests {
// - ID: archive-bubgo, Name: archive-bubgo
// - ID: archive-buzxy, Name: archive-buzxy
    private static ObjectClient fileClient;
    private static final String BUCKET_ID = "archive-bubgo"; // replace with real bucket ID
    private static final String FILE_ID="1761367756535272000";
    private static File testFile;

    @BeforeAll
    static void setup() throws Exception {
        ClientConfig config = new ClientConfig( "550e8400-e29b-41d4-a716-446655440000", 10);
        fileClient = new ObjectClient(new HttpClient(config));

        // Create dummy file if it doesn’t exist
        testFile = new File(System.getProperty("java.io.tmpdir"), "test_upload.txt");
        if (!testFile.exists()) {
            try (var writer = new java.io.FileWriter(testFile)) {
                writer.write("Hello, this is a test upload!");
            }
        }
        System.out.println("🧪 Using test file: " + testFile.getAbsolutePath());
    }




    @Test
    @Order(1)
    void testUploadFile() {
        try {
            FileInfo uploaded = fileClient.upload(BUCKET_ID, testFile);

            assertThat(uploaded).isNotNull();
            assertThat(uploaded.getKey()).isNotBlank();
            assertThat(uploaded.getSize()).isGreaterThan(0);

            System.out.println("✅ File uploaded successfully:");
            System.out.println("ID: " + uploaded.getId());
            System.out.println("Key: " + uploaded.getKey());
            System.out.println("Size: " + uploaded.getSize());
            System.out.println("MimeType: " + uploaded.getMimeType());
        } catch (Exception e) {
            Assertions.fail("❌ File upload failed: " + e.getMessage(), e);
        }
    }

    @Test
    @Order(2)
    void testListFiles() {
        try {
            List<FileInfo> files = fileClient.list(BUCKET_ID);

            assertThat(files).isNotNull();
            assertThat(files).isInstanceOf(List.class);

            System.out.println("✅ Files listed successfully:");
            System.out.println("Total files in bucket: " + files);
        } catch (Exception e) {
            Assertions.fail("❌ File listing failed: " + e.getMessage(), e);
        }
    }




    @Test
    @Order(3)
    void testGetFileMetadata() {
        try {
            FileInfo metadata = fileClient.get(BUCKET_ID, FILE_ID);

            assertThat(metadata).isNotNull();
            assertThat(metadata.getId()).isNotBlank();
            assertThat(metadata.getKey()).isNotBlank();
            assertThat(metadata.getSize()).isGreaterThan(0);
            assertThat(metadata.getBucketId()).isEqualTo(BUCKET_ID);
            assertThat(metadata.getMimeType()).isNotBlank();
            assertThat(metadata.getCreatedAt()).isNotBlank();
if (metadata.getMetadata() != null && !metadata.getMetadata().isEmpty()) {
                System.out.println("Custom Metadata: " + metadata.getMetadata());
            }

        } catch (Exception e) {
            Assertions.fail("❌ Get file metadata failed: " + e.getMessage(), e);
        }
    }



    @Test
    @Order(4)
    void testDownloadFile() {
        try {
            byte[] downloadedData = fileClient.download(BUCKET_ID, FILE_ID);

            assertThat(downloadedData).isNotNull();
            assertThat(downloadedData).isNotEmpty();

            System.out.println("✅ File downloaded successfully:");
            System.out.println("Downloaded size: " + downloadedData.length + " bytes");

        } catch (Exception e) {
            Assertions.fail("❌ File download failed: " + e.getMessage(), e);
        }
    }


    @Test
    @Order(5)
    void testDeleteFile() {
        try {
            fileClient.delete(BUCKET_ID, FILE_ID);
            System.out.println("✅ File deletion request sent successfully");
            // Verify file no longer exists by trying to get it
            assertThrows(Exception.class, () -> {
                fileClient.get(BUCKET_ID, FILE_ID);
            });
            System.out.println("✅ File confirmed deleted - no longer accessible");
        } catch (Exception e) {
            Assertions.fail("❌ File deletion failed: " + e.getMessage(), e);
        }
    }






    @Test
    @Order(6)
    void testUpdateFileMetadata() {
        try {
            // Create metadata update input
            UpdateFileMetadataInput input = UpdateFileMetadataInput.builder()
                    .metadata(Map.of(
                            "author", "Test User",
                            "category", "unit-test",
                            "updated_by", "sdk-test"
                    ))
                    .build();

            // Update file metadata
            FileInfo updatedFile = fileClient.updateMetadata("archive-buyyt", FILE_ID, input);

            assertThat(updatedFile).isNotNull();
            assertThat(updatedFile.getId()).isEqualTo(FILE_ID);
            assertThat(updatedFile.getBucketId()).isEqualTo(BUCKET_ID);
            assertThat(updatedFile.getMetadata()).isNotNull();
            assertThat(updatedFile.getMetadata()).containsKey("author");
            assertThat(updatedFile.getMetadata()).containsEntry("author", "Test User");
            assertThat(updatedFile.getMetadata()).containsEntry("category", "unit-test");

            System.out.println("✅ File metadata updated successfully:");
            System.out.println("File ID: " + updatedFile.getId());
            System.out.println("Updated metadata: " + updatedFile.getMetadata());

        } catch (Exception e) {
            Assertions.fail("❌ Update file metadata failed: " + e.getMessage(), e);
        }
    }


    @Test
    @Order(7)
    void testCopyFile() {
        try {
            // Create copy input
            CopyFileInput input = CopyFileInput.builder()
                    .targetBucketId(BUCKET_ID) // Copy within same bucket for simplicity
                    .targetKey("copied-" + System.currentTimeMillis() + ".jpg")
                    .build();

            // Copy the file
            FileInfo copiedFile = fileClient.copy(BUCKET_ID, FILE_ID, input);

            assertThat(copiedFile).isNotNull();
            assertThat(copiedFile.getId()).isNotEqualTo(FILE_ID); // Should be new file ID
            assertThat(copiedFile.getKey()).isEqualTo(input.getTargetKey());
            assertThat(copiedFile.getBucketId()).isEqualTo(BUCKET_ID);
            assertThat(copiedFile.getSize()).isGreaterThan(0);
            assertThat(copiedFile.getMimeType()).isNotBlank();

            System.out.println("✅ File copied successfully:");
            System.out.println("Original ID: " + FILE_ID);
            System.out.println("Copied ID: " + copiedFile.getId());
            System.out.println("New Key: " + copiedFile.getKey());
            System.out.println("Size: " + copiedFile.getSize());

        } catch (Exception e) {
            Assertions.fail("❌ File copy failed: " + e.getMessage(), e);
        }
    }

    @Test
    @Order(8)
    void testMoveFile() {
        try {
            // Create move input
            MoveFileInput input = MoveFileInput.builder()
                    .targetBucketId(BUCKET_ID) // Move within same bucket for simplicity
                    .targetKey("moved-" + System.currentTimeMillis() + ".jpg")
                    .build();

            String originalKey = fileClient.get(BUCKET_ID, FILE_ID).getKey();

            // Move the file
            FileInfo movedFile = fileClient.move(BUCKET_ID, FILE_ID, input);

            assertThat(movedFile).isNotNull();
            assertThat(movedFile.getId()).isEqualTo(FILE_ID); // Same file ID (updated record)
            assertThat(movedFile.getKey()).isEqualTo(input.getTargetKey());
            assertThat(movedFile.getKey()).isNotEqualTo(originalKey);
            assertThat(movedFile.getBucketId()).isEqualTo(BUCKET_ID);
            assertThat(movedFile.getSize()).isGreaterThan(0);

            System.out.println("✅ File moved successfully:");
            System.out.println("File ID: " + movedFile.getId());
            System.out.println("Original Key: " + originalKey);
            System.out.println("New Key: " + movedFile.getKey());
            System.out.println("Size: " + movedFile.getSize());

        } catch (Exception e) {
            Assertions.fail("❌ File move failed: " + e.getMessage(), e);
        }
    }

    @AfterAll
    static void cleanup() {
        if (testFile.exists()) {
            testFile.delete();
            System.out.println("🧹 Deleted temporary file.");
        }
    }
}
