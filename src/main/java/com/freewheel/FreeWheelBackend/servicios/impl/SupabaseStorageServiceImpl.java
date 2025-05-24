package com.freewheel.FreeWheelBackend.servicios.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freewheel.FreeWheelBackend.servicios.StorageService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseStorageServiceImpl.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    public SupabaseStorageServiceImpl() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName, String folderPath) throws IOException {
        logger.info("Iniciando carga de archivo: {} en carpeta: {}", fileName, folderPath);

        // Generate unique filename if none provided
        if (fileName == null || fileName.isEmpty()) {
            fileName = UUID.randomUUID().toString();
            logger.info("Generando nombre de archivo único: {}", fileName);
        }

        // Add file extension if it exists
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!fileName.endsWith(extension)) {
                fileName = fileName + extension;
                logger.info("Añadiendo extensión al archivo: {}", fileName);
            }
        }

        // Prepare the folder path (ensure it doesn't start or end with slash)
        String folder = "";
        if (folderPath != null && !folderPath.isEmpty()) {
            folder = folderPath.trim();
            if (folder.startsWith("/")) {
                folder = folder.substring(1);
            }
            if (!folder.endsWith("/")) {
                folder = folder + "/";
            }
        }

        logger.info("Preparando para subir archivo al bucket: {}, carpeta: {}", bucketName, folder);

        // Prepare the request
        RequestBody requestBody = RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType()));

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folder + fileName;
        logger.info("URL de carga: {}", uploadUrl);

        Request request = new Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", file.getContentType())
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            logger.info("Respuesta recibida, código: {}", response.code());

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                logger.error("Error al subir archivo: {} - {} - {}", response.code(), response.message(), errorBody);
                throw new IOException("Failed to upload file: " + response.code() + " - " + response.message() + " - " + errorBody);
            }

            String responseBody = response.body() != null ? response.body().string() : null;
            if (responseBody == null) {
                logger.error("El cuerpo de la respuesta está vacío");
                throw new IOException("Empty response body");
            }

            logger.info("Respuesta de Supabase: {}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + folder + fileName;

            logger.info("URL pública del archivo: {}", publicUrl);
            return publicUrl;
        } catch (Exception e) {
            logger.error("Excepción al subir archivo: ", e);
            throw e;
        }
    }
}