package com.example.domain.service;

import com.example.domain.model.LargeFile;

import java.io.IOException;
import java.io.OutputStream;

public interface LargeFileService {
    void upload(LargeFile largeFile);
    LargeFile download(String id,OutputStream downloadOutputStream) throws IOException;
}
