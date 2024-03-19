/*
 * Copyright 2023 Ant Group Co., Ltd.
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

import org.secretflow.easypsi.common.dto.DownloadInfo;
import org.secretflow.easypsi.common.errorcode.DataErrorCode;
import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * File utils
 *
 * @author yansi
 * @date 2023/5/4
 */
public class FileUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public final static String FILE_NAME = "FILE_NAME";

    public final static String FILE_PATH = "FILE_PATH";

    private final static List<String> SUPPORT_FILE_TYPE = Arrays.asList(
            ".csv",
            ".crt",
            ".cert");

    private final static String FILE_NAME_REGEX = "^[a-zA-Z0-9\\u4e00-\\u9fa5-—_()（）\\s]*$";

    private final static List<String> SUPPORT_FILE_PATH = Arrays.asList(
            "/app/data",
            "/app/log",
            "/app/log/pods",
            "/app/data/result",
            "/app/data/tmp");

    /**
     * Load file from the classpath resources or filesystem
     *
     * @param filepath path of a file
     *                 Example:
     *                 1. classpath:./a.txt
     *                 2. file:./config/a.txt
     *                 3. ./config/a.txt
     * @return File
     * @throws FileNotFoundException
     */
    public static File readFile(String filepath) throws FileNotFoundException {
        File file = ResourceUtils.getFile(filepath);
        if (!file.exists()) {
            throw new FileNotFoundException(filepath);
        }
        return file;
    }

    /**
     * Load file from the classpath resources or filesystem and return string
     *
     * @param filepath
     * @return String
     * @throws IOException
     */
    public static String readFile2String(String filepath) throws IOException {
        return readFile2String(readFile(filepath));
    }

    /**
     * Load file from the file and return string
     *
     * @param file
     * @return String
     * @throws IOException
     */
    public static String readFile2String(File file) throws IOException {
        return FileCopyUtils.copyToString(new FileReader(file));
    }

    /**
     * Delete all files in the directory
     *
     * @param dir target directory
     * @return if all files in the directory deleted
     */
    public static boolean deleteAllFile(String dir) {
        File dirFile = new File(dir);
        // If the file corresponding to dir does not exist or is not a directory, exit
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                // Delete a subFile
                if (file.isFile()) {
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
                // Delete a subDirectory
                else if (file.isDirectory()) {
                    flag = deleteAllFile(file.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Delete a single file
     *
     * @param fileName target file name
     * @return if the single file deleted
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // if the file path is only a single file
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                LOGGER.info("Delete single file:{} Successfully!", fileName);
                return true;
            } else {
                LOGGER.info("Delete single file:{} Failed!", fileName);
                return false;
            }
        } else {
            LOGGER.info("File {} not exists!", fileName);
            return false;
        }
    }

    /**
     * based on file path and end return file name or path
     *
     * @param directory
     * @param nameEnd
     * @param type
     * @return
     */
    public static List<String> traverseDirectories(File directory, String nameEnd, String type) {
        if (!directory.exists()) {
            return null;
        }
        List<String> filePath = new ArrayList<>();
        if (!ObjectUtils.isEmpty(directory) && directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isDirectory() && file.getName().endsWith(nameEnd)) {
                        if (FILE_PATH.equals(type)) {
                            filePath.add(directory.getPath() + File.separator + file.getName());
                        } else if (FILE_NAME.equals(type)) {
                            filePath.add(file.getName());
                        }
                    }
                }
            }
        }
        return filePath;
    }

    /**
     * Build download information
     *
     * @param filePath    download file path
     * @param dir         download directory
     * @param relativeUri download relativeUri
     * @return download information
     */
    public static DownloadInfo download(String filePath, String dir, String relativeUri) {
        File f = new File(filePath);
        try {
            if (!f.exists()) {
                LOGGER.warn("The result relative uri file {} not exits.", filePath);
                // Todo: the result so far is that an empty file is returned if it does not exist
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                if (!f.createNewFile()) {
                    LOGGER.error("failed to create empty file.");
                    throw EasyPsiException.of(SystemErrorCode.UNKNOWN_ERROR, "failed to create empty file for return.");
                }
            }
            String downloadFilePath = null;
            String fileName = null;
            if (f.isDirectory()) {
                LOGGER.info("Download process got a dir to download, whose relative uri = {}", relativeUri);
                CompressUtils.compress(filePath, dir, relativeUri);
                fileName = relativeUri.contains(".tar.gz") ? relativeUri : relativeUri + ".tar.gz";
                // since it is a new compressed file, add a suffix
                downloadFilePath = dir + fileName;

            } else {
                LOGGER.info("Download process got a  real csv file to download, whose relative uri = {}", relativeUri);
                fileName = relativeUri.contains(".csv") ? relativeUri : relativeUri + ".csv";
                // since the source file is already csv, there is no need to add a suffix, but the file name returned above is suffixed
                downloadFilePath = dir + relativeUri;
            }
            LOGGER.info("When download, the relative uri = {}. the real file path = {}", relativeUri, filePath);
            return DownloadInfo.builder()
                    .fileName(fileName)
                    .filePath(downloadFilePath)
                    .build();
        } catch (IOException e) {
            LOGGER.error("IO exception: {}", e.getMessage());
            throw EasyPsiException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        } catch (Exception e) {
            LOGGER.error("got Exception: {}", e.getMessage());
            throw EasyPsiException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
    }

    public static void fileNameCheck(String fileName) {
        fileNameCheck(fileName, null);
    }

    public static void fileNameCheck(String fileName, String regex) {
        if (ObjectUtils.isEmpty(fileName)) {
            LOGGER.error("The user input fileName {} is empty!", fileName);
            throw EasyPsiException.of(DataErrorCode.FILE_NAME_EMPTY);
        }

        Pattern pattern = Pattern.compile(ObjectUtils.isEmpty(regex) ? FILE_NAME_REGEX : regex);
        String prefixName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        if (!pattern.matcher(prefixName).matches()) {
            LOGGER.error("The user input filName {} contains dangerous characters", fileName);
            throw EasyPsiException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR, fileName);
        }

        if (!fileName.contains(".")) {
            return;
        }

        String suffixName = fileName.substring(fileName.lastIndexOf('.'));
        if (!SUPPORT_FILE_TYPE.contains(suffixName)) {
            LOGGER.error("The user input fileName {} type {} not support yet.", fileName, suffixName);
            throw EasyPsiException.of(DataErrorCode.FILE_TYPE_NOT_SUPPORT, suffixName);
        }
    }

    public static void filePathCheck(String filePath) {
        if (ObjectUtils.isEmpty(filePath)) {
            LOGGER.error("The user input filePath {} is empty!", filePath);
            throw EasyPsiException.of(DataErrorCode.FILE_PATH_EMPTY);
        }
        if (!SUPPORT_FILE_PATH.contains(filePath)) {
            LOGGER.error("The user input filPath {} not in whitelist ", filePath);
            throw EasyPsiException.of(DataErrorCode.FILE_PATH_ERROR, filePath);
        }
    }

}
