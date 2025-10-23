package org.example;

import org.example.config.ClientConfig;
import org.example.dto.bucket.*;
import org.example.exceptions.ApiExceptions;
import org.example.http.HttpClient;
import org.example.services.bucket.FileClient;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class S3EndToEndTest {

    public static void main(String[] args) {
        ClientConfig config = new ClientConfig("http://localhost:8080", "550e8400-e29b-41d4-a716-446655440000", 10);
        S3CloneClient client = new S3CloneClient(config);
        HttpClient httpClient = new HttpClient(config);
        FileClient fileClient = new FileClient(httpClient);

        Bucket createdBucket = null;

        try {
            // -------------------------
            // 1️⃣ CREATE BUCKET
            // -------------------------
            CreateBucketInput createInput = new CreateBucketInput();
            String bucketName = "archive-bu" + randomSuffix();
            createInput.setName(bucketName);
            createInput.setOwnerId("550e8400-e29b-41d4-a716-446655440000");

            try {
                createdBucket = client.buckets().create(createInput);
                System.out.println("✅ Bucket created: " + createdBucket.getBucketId());
            } catch (ApiExceptions.ConflictException e) {
                System.err.println("⚠️ Bucket already exists: " + e.getResponseBody());
            }

//            // -------------------------
//            // 2️⃣ LIST BUCKETS
//            // -------------------------
//            List<Bucket> buckets = client.buckets().list();
//            System.out.println("📦 Total buckets: " + buckets.size());
//
//            // -------------------------
//            // 3️⃣ GET BUCKET INFO
//            // -------------------------
//            if (createdBucket != null) {
//                Bucket b = client.buckets().get(createdBucket.getBucketId());
//                System.out.println("ℹ️ Got bucket: " + b.getName());
//            }
//
//            // -------------------------
//            // 4️⃣ UPDATE BUCKET
//            // -------------------------
//            if (createdBucket != null) {
//                UpdateBucketInput updateInput = new UpdateBucketInput();
//                updateInput.setName("bt-" + bucketName);
//                Bucket updated = client.buckets().update(createdBucket.getBucketId(), updateInput);
//                System.out.println("📝 Updated bucket name: " + updated.getName());
//            }

            // -------------------------
            // 5️⃣ POLICY MANAGEMENT
            // -------------------------
            // 1. Ensure a bucket was created
            if (createdBucket != null) {
                String bucketId = createdBucket.getBucketId();

                // 2. Construct a policy input
                UpdatePolicyInput policyInput = new UpdatePolicyInput();
                policyInput.setPublicRead(true);
                policyInput.setPublicWrite(false);
                policyInput.setVersion("1");
                policyInput.setEffect("Allow");
                policyInput.setActions(Arrays.asList("upload", "delete"));
                policyInput.setResources(List.of("bucket/*"));
                policyInput.setPrincipals(List.of("user:123"));

                // 3. Send the update request
                client.buckets().updatePolicy(bucketId, policyInput);
                System.out.println("🔐 Bucket policy updated.");

                // 4. Fetch back the current policy
                PolicyResponse  currentPolicy = client.buckets().getPolicy(bucketId);

                // 5. Print what the server reports
                System.out.println("📜 Policy - Public Read: " + currentPolicy.getPolicy() +
                        ", Public Write: " + currentPolicy.getName());

            }


//            // -------------------------
//            // 6️⃣ VERSIONING
//            // -------------------------
//            if (createdBucket != null) {
//                String bucketId = createdBucket.getBucketId();
//
//                // Enable versioning
//                VersioningInput versioning = new VersioningInput();
//                versioning.setEnabled(true);
//                client.buckets().setVersioning(bucketId, versioning);
//                System.out.println("🗂️ Versioning enabled.");
//
//                // Fetch versioning state
//                VersioningOutput current = client.buckets().getVersioning(bucketId);
//                System.out.println("📄 Versioning enabled: " + current.getStatus());
//            }
//
//            // -------------------------
//            // 7️⃣ LIFECYCLE
//            // -------------------------
            if (createdBucket != null) {
                String bucketId = createdBucket.getBucketId();

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
//
//            // -------------------------
//            // 8️⃣ FILE UPLOAD
//            // -------------------------
//            if (createdBucket != null) {
//                try {
//                    File file = new File("C:\\Users\\6D617274696E\\Downloads\\burpsuite_community_windows-x64_v2025_9_5.exe");
//                    if (!file.exists()) {
//                        System.out.println("Creating dummy test file...");
//                        try (var writer = new java.io.FileWriter(file)) {
//                            writer.write("Hello, this is a test upload!");
//                        }
//                    }
//                    System.out.println("Uploading file to bucket: " + createdBucket.getName());
//
//                    // Uncomment when FileClient.upload() is ready
//                    // FileInfo uploaded = fileClient.upload(createdBucket.getBucketId(), file);
//                    // System.out.println("✅ File uploaded successfully:");
//                    // System.out.println("ID: " + uploaded.getId());
//                    // System.out.println("Key: " + uploaded.getKey());
//                    // System.out.println("Size: " + uploaded.getSize());
//                    // System.out.println("MimeType: " + uploaded.getMimeType());
//                } catch (Exception e) {
//                    System.err.println("❌ Error during file upload: " + e);
//                    e.printStackTrace();
//                }
//            }
//
//            // -------------------------
//            // 9️⃣ BUCKET STATS
//            // -------------------------
//            if (createdBucket != null) {
//                BucketStats stats = client.buckets().stats(createdBucket.getBucketId());
//                System.out.println("📊 Total files in bucket: " + stats.getTotalFiles());
//            }
//
//            // -------------------------
//            // 🔟 CLEANUP (DELETE BUCKET)
//            // -------------------------
//            if (createdBucket != null) {
//                client.buckets().delete(createdBucket.getBucketId());
//                System.out.println("🗑️ Bucket deleted successfully.");
//            }

        } catch (Exception e) {
            System.err.println("❌ Error during end-to-end test: " + e);
            e.printStackTrace();
        }
    }

    // Helper to generate random 3-letter suffix
    private static String randomSuffix() {
        Random random = new Random();
        return random.ints(3, 0, 26)
                .mapToObj(i -> String.valueOf((char) ('a' + i)))
                .reduce("", String::concat);
    }
}
