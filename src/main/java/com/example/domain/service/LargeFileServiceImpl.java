package com.example.domain.service;

import com.example.domain.model.LargeFile;
import com.example.domain.repository.LargeFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Transactional
@Service
public class LargeFileServiceImpl implements LargeFileService {

    @Inject
    LargeFileRepository largeFileRepository;

    public void upload(LargeFile largeFile) {
        largeFileRepository.save(largeFile);
    }

    public LargeFile download(String id, OutputStream downloadOutputStream) throws IOException {
        LargeFile largeFile = largeFileRepository.findOne(id);
        try (InputStream content = largeFile.getContent()) {
            StreamUtils.copy(content, downloadOutputStream);
        }
        return largeFile;
    }

}
