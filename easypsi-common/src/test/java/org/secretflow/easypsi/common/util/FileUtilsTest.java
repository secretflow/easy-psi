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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.secretflow.easypsi.common.exception.EasyPsiException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * File Utils Test
 *
 * @author lihaixin
 * @date 2024/03/08
 */
public class FileUtilsTest {

    @TempDir
    Path tempDir;

    private static final String FILE_CONTENT = "Hello, World!";

    @Test
    public void readFileTest() throws IOException {
        Path path = Files.writeString(tempDir.resolve("test.txt"), FILE_CONTENT);
        FileUtils.readFile(path.toFile().getPath());
        FileUtils.readFile2String(path.toFile().getPath());
        FileUtils.readFile2String(path.toFile());
        Assertions.assertThrows(FileNotFoundException.class, () -> FileUtils.readFile("invalid_path.txt"), "File not found ");
        Assertions.assertThrows(FileNotFoundException.class, () -> FileUtils.readFile2String("invalid_path.txt"), "File not found ");
        Assertions.assertThrows(FileNotFoundException.class, () -> FileUtils.readFile2String(new File("invalid_path.txt")), "File not found ");
    }

    @Test
    public void deleteFileTest() throws IOException {
        Files.writeString(tempDir.resolve("test.txt"), FILE_CONTENT);
        FileUtils.deleteFile(tempDir.toString());
        Files.writeString(tempDir.resolve("test.txt"), FILE_CONTENT);
        FileUtils.deleteAllFile(tempDir.toString());
    }

    @Test
    public void traverseDirectoriesTest() throws IOException {
        Files.writeString(tempDir.resolve("test.txt"), FILE_CONTENT);
        File directory = tempDir.toFile();
        Assertions.assertTrue(directory.exists() && directory.isDirectory());
        List<String> filePaths = FileUtils.traverseDirectories(directory, ".txt", FileUtils.FILE_PATH);
        Assertions.assertNotNull(filePaths);
        Assertions.assertFalse(filePaths.isEmpty());
    }

    @Test
    public void downloadTest() throws IOException {
        String filePath = tempDir.resolve("test.csv").toString();
        File realFile = new File(filePath);
        realFile.createNewFile();
        String dir = tempDir.toString();
        String relativeUri = "data.csv";
        FileUtils.download(filePath, dir, relativeUri);

    }

    @Test
    public void fileNameCheckTest() {
        // Valid case
        FileUtils.fileNameCheck("valid_filename.csv");

        // Empty filename check
        Assertions.assertThrows(EasyPsiException.class, () -> FileUtils.fileNameCheck(""), "Expected exception for empty filename");

        // Dangerous character check
        Assertions.assertThrows(EasyPsiException.class, () -> FileUtils.fileNameCheck("invalid!filename.csv"), "Expected exception for dangerous characters");

        // Unsupported file type check
        Assertions.assertThrows(EasyPsiException.class, () -> FileUtils.fileNameCheck("file.name.xls"), "Expected exception for unsupported file type");

        Assertions.assertThrows(EasyPsiException.class, () -> FileUtils.fileNameCheck("valid_filename.csv1"), "Expected exception for unsupported file type");

    }
    @Test
    public void testFilePathCheck() {
        // Valid case
        FileUtils.filePathCheck("/app/data");

        // Empty filepath check
        Assertions.assertThrows(EasyPsiException.class, () -> FileUtils.filePathCheck(""), "Expected exception for empty filepath");

        // Invalid filepath check
        Assertions.assertThrows(EasyPsiException.class, () -> FileUtils.filePathCheck("/invalid/path"), "Expected exception for invalid filepath");
    }
}
