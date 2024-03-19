/*
 * Copyright 2024 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.easypsi.common.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.CompressUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compress Utils Test
 *
 * @author lihaixin
 * @date 2024/03/07
 */
public class CompressUtilsTest {

    @TempDir
    Path tempDir;

    private static final String FILE_CONTENT = "Hello, World!";

    @BeforeEach
    void setUp() throws IOException {

    }

    @Test
    public void getFilesTest() throws Exception {
        Files.createDirectories(tempDir.resolve("dir1/dir2"));
        Files.writeString(tempDir.resolve("file1.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/file2.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/dir2/file3.txt"), FILE_CONTENT);
        CompressUtils.getFiles(tempDir.toString());
    }

    @Test
    public void packTest() throws IOException {
        Files.createDirectories(tempDir.resolve("dir1/dir2"));
        Files.writeString(tempDir.resolve("file1.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/file2.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/dir2/file3.txt"), FILE_CONTENT);
        List<File> fileList = CompressUtils.getFiles(tempDir.toAbsolutePath().toString());
        File targetArchive = new File(tempDir.toString() + "/test_archive.tar"); //new File(tempDirPath).toPath().resolve("test_archive.tar").toFile()
        CompressUtils.pack(fileList, tempDir.toString(), targetArchive);
    }


    @Test
    public void compressTest() throws Exception {
        Files.createDirectories(tempDir.resolve("dir1/dir2"));
        Files.writeString(tempDir.resolve("file1.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/file2.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/dir2/file3.txt"), FILE_CONTENT);
        CompressUtils.compress(tempDir.toString(), tempDir.toString(), "test.tar");
    }

    @Test
    public void compressTarTest() throws Exception {
        Files.createDirectories(tempDir.resolve("dir1/dir2"));
        Files.writeString(tempDir.resolve("file1.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/file2.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/dir2/file3.txt"), FILE_CONTENT);
        List<File> fileList = CompressUtils.getFiles(tempDir.toAbsolutePath().toString());
        CompressUtils.compressTar(fileList, tempDir.toString(), tempDir.toString(), "test.tar");
    }

    @Test
    public void decompressTest() throws Exception {
        Files.createDirectories(tempDir.resolve("dir1/dir2"));
        Files.writeString(tempDir.resolve("file1.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/file2.txt"), FILE_CONTENT);
        Files.writeString(tempDir.resolve("dir1/dir2/file3.txt"), FILE_CONTENT);
        Assertions.assertThrows(EasyPsiException.class, () -> CompressUtils.decompress(tempDir.toString(), tempDir.toString()));
    }

    @Test
    public void unZipTest() throws Exception {
        Files.writeString(tempDir.resolve("file1.txt"), FILE_CONTENT);
        Path tempZipFile = tempDir.resolve("mocked.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZipFile))) {
            ZipEntry entry1 = new ZipEntry("file1.txt");
            zos.putNextEntry(entry1);
            zos.write("This is file 1 in the ZIP".getBytes());
            zos.closeEntry();
        }
        File mockZipFile = tempZipFile.toFile();
        CompressUtils.unZip(mockZipFile, tempDir.toString());
    }

    @Test
    public void decompressTarGzhTest() throws Exception {
        File compressedTarFile = new File(tempDir.toFile(), "test.tar.gz");
        try (FileOutputStream fos = new FileOutputStream(compressedTarFile);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos);
             TarArchiveOutputStream tao = new TarArchiveOutputStream(gzos)) {
            TarArchiveEntry dirEntry = new TarArchiveEntry("dir1");
            dirEntry.setSize(0);
            dirEntry.setMode(TarArchiveEntry.DEFAULT_DIR_MODE);
            tao.putArchiveEntry(dirEntry);
            tao.closeArchiveEntry();
            tao.finish();
        }
        String outputDirStr = tempDir.resolve("output").toString();
        CompressUtils.decompressTarGz(compressedTarFile, outputDirStr);
    }


    @Test
    public void decompressTarBz2Test() throws Exception {
        File compressedTarFile = new File(tempDir.toFile(), "test.tar.bz2");
        try (FileOutputStream fos = new FileOutputStream(compressedTarFile);
             BZip2CompressorOutputStream bz2os = new BZip2CompressorOutputStream(fos);
             TarArchiveOutputStream tao = new TarArchiveOutputStream(bz2os)) {
            TarArchiveEntry dirEntry = new TarArchiveEntry("dir1");
            dirEntry.setSize(0);
            dirEntry.setMode(TarArchiveEntry.DEFAULT_DIR_MODE);
            tao.putArchiveEntry(dirEntry);
            tao.closeArchiveEntry();
            tao.finish();
        }
        String outputDirStr = tempDir.resolve("output").toString();
        CompressUtils.decompressTarBz2(compressedTarFile, outputDirStr);
    }

    @Test
    public void writeFileTest() throws Exception {
        InputStream in = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressUtils.writeFile(in, out);
    }


    @Test
    public void createDirectoryTest() throws Exception {
        String outputDir = tempDir + "/directory1";
        CompressUtils.createDirectory(outputDir, null);
    }

}