package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.config.ClientConfig;
import org.example.dto.bucket.*;
import org.example.exceptions.ApiExceptions;
import org.example.services.bucket.BucketClient;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BucketsIntegrationTest {

    private static final String BUCKET_ID = "archive-bubgo";
    private static S3CloneClient client;
    private static String bucketId;

    @BeforeAll
    static void setup() {
        ClientConfig config = new ClientConfig(

                "550e8400-e29b-41d4-a716-446655440000",
                10
        );
        client = new S3CloneClient(config);
    }

    private static String randomSuffix() {
        Random random = new Random();
        return random.ints(3, 0, 26)
                .mapToObj(i -> String.valueOf((char) ('a' + i)))
                .reduce("", String::concat);
    }

    @Test
    @Order(1)
    void testCreateBucket() throws JsonProcessingException {
        CreateBucketInput input = new CreateBucketInput();
        input.setName("archive-bu" + randomSuffix());
        input.setOwnerId("550e8400-e29b-41d4-a716-446655440000");

        Bucket bucket = client.buckets().create(input);
        assertThat(bucket).isNotNull();
        assertThat(bucket.getName()).startsWith("archive-bu");
        bucketId = bucket.getBucketId();

        System.out.println("✅ Bucket created: " + bucketId);
    }

    @Test
    @Order(2)
    void testListBuckets() throws JsonProcessingException {
        List<Bucket> buckets = client.buckets().list();
        assertThat(buckets).isNotEmpty();
        System.out.println("📦 Total buckets: " + buckets.size());
    }

    @Test
    @Order(3)
    void testGetBucketInfo() throws ApiExceptions, JsonProcessingException {
        Bucket b = client.buckets().get(bucketId);
        assertThat(b).isNotNull();
        assertThat(b.getBucketId()).isEqualTo(bucketId);
        System.out.println("ℹ️ Got bucket: " + b.getName());
    }

    @Test
    @Order(4)
    void testUpdatePolicy() throws JsonProcessingException {
        UpdatePolicyInput input = new UpdatePolicyInput();
        input.setVersion("1");
        input.setEffect("Allow");
        input.setActions(Arrays.asList("upload", "delete"));
        input.setResources(List.of("bucket/*"));
        input.setPrincipals(List.of("user:123"));
        input.setPublicRead(true);
        input.setPublicWrite(false);

        client.buckets().updatePolicy(bucketId, input);
        PolicyResponse policy = client.buckets().getPolicy(bucketId);

        assertThat(policy).isNotNull();
        assertThat(policy.getPolicy()).contains("upload");
        System.out.println("🔐 Bucket policy updated.");
    }

    @Test
    @Order(5)
    void testLifecycleRules() throws JsonProcessingException {
        LifecycleRuleInput rule = new LifecycleRuleInput(
                "rule1",
                "logs/",
                "Enabled",
                30,
                10,
                "STANDARD_IA"
        );
        LifecycleInput input = new LifecycleInput(List.of(rule));

        client.buckets().setLifecycle(bucketId, input);
        System.out.println("🕓 Lifecycle policy set (30 days).");
    }

    @Test
    @Order(6)
    void testBucketStats() throws JsonProcessingException {
        BucketStats stats = client.buckets().stats(bucketId);
        assertThat(stats).isNotNull();
        System.out.println("📊 Total files in bucket: " + stats.getTotalFiles());
    }

    @AfterAll
    static void cleanup() throws JsonProcessingException {
        if (bucketId != null) {
            client.buckets().delete(bucketId);
            System.out.println("🗑️ Bucket deleted successfully.");
        }
    }

    @Test
    @Order(7)
    @DisplayName("Enable bucket versioning")
    void testEnableVersioning() {
        try {
            VersioningInput input = new VersioningInput();
            input.setEnabled(true);

            client.buckets().setVersioning(BUCKET_ID, input);
            System.out.println("✅ Versioning enabled for bucket " + BUCKET_ID);

            // Fetch and verify
            VersioningOutput output = client.buckets().getVersioning(BUCKET_ID);
            assertThat(output).isNotNull();
            assertThat(output.getStatus()).isEqualTo("Enabled");

            System.out.println("📄 Versioning status: " + output.getStatus());
        } catch (Exception e) {
            Assertions.fail("❌ Failed to enable versioning: " + e.getMessage(), e);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Disable bucket versioning")
    void testDisableVersioning() {
        try {
            VersioningInput input = new VersioningInput();
            input.setEnabled(false);

            BucketClient bucketClient;
            client.buckets().setVersioning(BUCKET_ID, input);
            System.out.println("✅ Versioning disabled for bucket " + BUCKET_ID);

            VersioningOutput output = client.buckets().getVersioning(BUCKET_ID);
            assertThat(output).isNotNull();
            assertThat(output.getStatus()).isEqualTo("Suspended");

            System.out.println("📄 Versioning status after disable: " + output.getStatus());
        } catch (Exception e) {
            Assertions.fail("❌ Failed to disable versioning: " + e.getMessage(), e);
        }
    }
}
