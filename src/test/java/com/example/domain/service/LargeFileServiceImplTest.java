package com.example.domain.service;

import com.example.domain.model.LargeFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import javax.inject.Inject;
import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class LargeFileServiceImplTest {

    @Inject
    LargeFileService largeFileService;

    private File uploadFile = new File("/work/files/input/terasoluna-server4jweb-doc_2.0.5.2.zip");

    private File downloadFile = new File("/work/files/output/download.zip");

    @Before
    public void setup() {
        if (!downloadFile.getParentFile().exists()) {
            downloadFile.getParentFile().mkdirs();
        }
        if (downloadFile.exists()) {
            downloadFile.delete();
        }
    }

    /**
     * Testing that access(insert/select) the blob data within the OID column of PostgreSQL.
     */
    @Test
    public void blob() throws IOException {

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


    /**
     * Testing that not occur the OutOfMemoryError.
     */
    @Test
    public void blobWithinRepeating() throws IOException {

        final long beforeFreeMemory = Runtime.getRuntime().freeMemory();
        final long beforeTotalMemory = Runtime.getRuntime().totalMemory();

        final int repeatCount = 100;
        final int poolCount = 10;
        final Executor executor = Executors.newFixedThreadPool(poolCount);
        final CountDownLatch countDownLatch = new CountDownLatch(poolCount);

        // upload in database
        final LargeFile newLargeFile = new LargeFile();
        try (InputStream uploadFileInputStream = new FileInputStream(uploadFile)) {
            newLargeFile.setFileName(uploadFile.getCanonicalPath());
            newLargeFile.setContent(uploadFileInputStream);
            largeFileService.upload(newLargeFile);
        }

        // download from database within repeating
        final Map<String, LargeFile> largeFileMap = new ConcurrentHashMap<>();
        for (int i = 0; i < poolCount; i++) {
            final File downloadFileForRepeating = new File(downloadFile.getParent(), "download" + i + ".zip");
            downloadFileForRepeating.delete();
            final Runnable command = new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < (repeatCount / poolCount); i++) {
                            try (OutputStream downloadFileOutputStream = new FileOutputStream(downloadFileForRepeating)) {

                                System.out.println("★★★★★ downloading of " + downloadFileForRepeating + " is started.");

                                LargeFile loadedLargeFile = largeFileService.download(newLargeFile.getId(), downloadFileOutputStream);

                                System.out.println("InputStream : " + loadedLargeFile.getContent().getClass().getName());
                                System.out.println("ID : " + loadedLargeFile.getId());
                                System.out.println("File Name : " + loadedLargeFile.getFileName());

                                assertThat(loadedLargeFile.getId(), is(newLargeFile.getId()));
                                assertThat(loadedLargeFile.getFileName(), is(uploadFile.getCanonicalPath()));

                                largeFileMap.put(UUID.randomUUID().toString(), loadedLargeFile);

                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            } finally {
                                System.out.println("★★★★★ downloading of " + downloadFileForRepeating + " is finished.");
                            }
                        }

                    } finally {
                        countDownLatch.countDown();
                    }
                }
            };
            executor.execute(command);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long afterFreeMemory = Runtime.getRuntime().freeMemory();
        final long afterTotalMemory = Runtime.getRuntime().totalMemory();

        System.out.println("★★★★★ Downloading is finished.");
        System.out.println("--- FREE MEMORY ---");
        System.out.println("before : " + (beforeFreeMemory / 1024 / 1024) + " MB");
        System.out.println("after  : " + (afterFreeMemory / 1024 / 1024) + " MB");
        System.out.println("--- TOTAL MEMORY ---");
        System.out.println("before : " + (beforeTotalMemory / 1024 / 1024) + " MB");
        System.out.println("after  : " + (afterTotalMemory / 1024 / 1024) + " MB");

        assertThat(largeFileMap.size(), is(repeatCount));
        // less than + 200MB
        assertThat(afterTotalMemory, is(lessThan(beforeTotalMemory + (200 * 1024 * 1024))));

        largeFileMap.clear();

    }

}