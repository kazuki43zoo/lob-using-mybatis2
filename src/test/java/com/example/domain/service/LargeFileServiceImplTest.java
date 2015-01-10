package com.example.domain.service;

import com.example.domain.model.LargeFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import javax.inject.Inject;
import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class LargeFileServiceImplTest {

    @Inject
    LargeFileService largeFileService;

    @Test
    public void blob() throws IOException {

        File uploadFile = new File("/work/files/input/terasoluna-server4jweb-doc_2.0.5.2.zip");

        File downloadFile = new File("/work/files/output/download.zip");
        if (!downloadFile.getParentFile().exists()) {
            downloadFile.getParentFile().mkdirs();
        }
        if (downloadFile.exists()) {
            downloadFile.delete();
        }

        // upload in database
        LargeFile newLargeFile = new LargeFile();
        try (InputStream uploadFileInputStream = new FileInputStream(uploadFile)) {
            newLargeFile.setFileName(uploadFile.getCanonicalPath());
            newLargeFile.setContent(uploadFileInputStream);
            largeFileService.upload(newLargeFile);
        }

        // download from database
        try (OutputStream downloadFileOutputStream = new FileOutputStream(downloadFile)) {

            LargeFile loadedLargeFile = largeFileService.download(newLargeFile.getId(), downloadFileOutputStream);

            System.out.println("InputStream : " + loadedLargeFile.getContent().getClass().getName());
            System.out.println("ID : " + loadedLargeFile.getId());
            System.out.println("File Name : " + loadedLargeFile.getFileName());

            assertThat(loadedLargeFile.getContent().getClass().getName(), is(not(ByteArrayInputStream.class.getName())));
            assertThat(loadedLargeFile.getId(), is(newLargeFile.getId()));
            assertThat(loadedLargeFile.getFileName(), is(uploadFile.getCanonicalPath()));
        }

        // assert file content
        try (InputStream uploadFileInputStream = new FileInputStream(uploadFile); InputStream downloadFileInputStream = new FileInputStream(downloadFile)) {
            byte[] uploadData = StreamUtils.copyToByteArray(uploadFileInputStream);
            byte[] downloadData = StreamUtils.copyToByteArray(downloadFileInputStream);
            assertThat(downloadData.length, is(uploadData.length));
            assertArrayEquals(uploadData, downloadData);
        }

    }

}
