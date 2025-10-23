package org.example;


import org.example.config.ClientConfig;

import org.example.dto.files.FileInfo;
import org.example.http.HttpClient;
import org.example.services.bucket.FileClient;

import java.io.File;

public class FileUploadTest {

    public static void main(String[] args) {
        try {
            // Configure client
            ClientConfig config = new ClientConfig("http://localhost:8080", null, 10);
            HttpClient httpClient = new HttpClient(config);
            FileClient fileClient = new FileClient(httpClient);

            // File to upload
            File file = new File("C:\\Users\\6D617274696E\\Downloads\\burpsuite_community_windows-x64_v2025_9_5.exe");
            if (!file.exists()) {
                System.out.println("Creating dummy test file...");
                try (var writer = new java.io.FileWriter(file)) {
                    writer.write("Hello, this is a test upload!");
                }
            }

            String bucketId = "archive-renakeekmedarchive-bugqx"; // replace with an actual bucket ID
            FileInfo uploaded = fileClient.upload(bucketId, file);

            System.out.println("File uploaded successfully:");
            System.out.println("ID: " + uploaded.getId());
            System.out.println("Key: " + uploaded.getKey());
            System.out.println("Size: " + uploaded.getSize());
            System.out.println("MimeType: " + uploaded.getMimeType());

        } catch (Exception e) {
            System.err.println("Error during file upload:"+e);
            e.printStackTrace();
        }
    }
}
