package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.config.ClientConfig;

import org.example.dto.bucket.Bucket;
import org.example.dto.bucket.CreateBucketInput;
import org.example.dto.files.FileInfo;
import org.example.http.HttpClient;
import org.example.services.bucket.FileClient;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilesIntegrationTests {

    private static FileClient fileClient;
    private static final String BUCKET_ID = "archive-bubgo"; // replace with real bucket ID
    private static File testFile;

    @BeforeAll
    static void setup() throws Exception {
        ClientConfig config = new ClientConfig("http://localhost:8080", null, 10);
        fileClient = new FileClient(new HttpClient(config));

        // Create dummy file if it doesn’t exist
        testFile = new File(System.getProperty("java.io.tmpdir"), "test_upload.txt");
        if (!testFile.exists()) {
            try (var writer = new java.io.FileWriter(testFile)) {
                writer.write("Hello, this is a test upload!");
            }
        }
        System.out.println("🧪 Using test file: " + testFile.getAbsolutePath());
    }

    private static String randomSuffix() {
        Random random = new Random();
        return random.ints(3, 0, 26)
                .mapToObj(i -> String.valueOf((char) ('a' + i)))
                .reduce("", String::concat);
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

    @AfterAll
    static void cleanup() {
        if (testFile.exists()) {
            testFile.delete();
            System.out.println("🧹 Deleted temporary file.");
        }
    }
}
