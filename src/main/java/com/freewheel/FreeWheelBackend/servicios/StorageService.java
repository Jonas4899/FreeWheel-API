package com.freewheel.FreeWheelBackend.servicios;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    /**
     * Upload a file to storage
     * @param file the file to upload
     * @param fileName the name to save the file as
     * @return the URL to access the file
     * @throws IOException if the file cannot be read
     */
    //String uploadFile(MultipartFile file, String fileName) throws IOException;
    String uploadFile(MultipartFile file, String fileName, String folderPath) throws IOException;
} 