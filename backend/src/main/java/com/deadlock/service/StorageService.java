package com.deadlock.service;

import java.util.List;

public interface StorageService {

    void uploadFile(String key, byte[] data, String contentType);

    byte[] downloadFile(String key);

    List<String> listFiles(String prefix);

    void deleteFiles(String prefix);
}
