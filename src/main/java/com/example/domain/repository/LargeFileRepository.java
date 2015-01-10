package com.example.domain.repository;

import com.example.domain.model.LargeFile;

public interface LargeFileRepository {
    LargeFile save(LargeFile largeFile);
    LargeFile findOne(String id);
}
