package com.example.domain.repository;

import com.example.domain.model.LargeFile;
import jp.terasoluna.fw.dao.QueryDAO;
import jp.terasoluna.fw.dao.UpdateDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.UUID;

@Transactional
@Repository
public class LargeFileRepositoryImpl implements LargeFileRepository {

    @Inject
    QueryDAO queryDAO;

    @Inject
    UpdateDAO updateDAO;

    public LargeFile save(LargeFile largeFile) {
        if (largeFile.getId() == null) {
            largeFile.setId(UUID.randomUUID().toString());
            updateDAO.execute("com.example.domain.repository.LargeFileRepository.create", largeFile);
        } else {
            throw new UnsupportedOperationException("updating is not supported.");
        }
        return largeFile;
    }

    public LargeFile findOne(String id) {
        return queryDAO.executeForObject("com.example.domain.repository.LargeFileRepository.findOne", id, LargeFile.class);
    }

}
