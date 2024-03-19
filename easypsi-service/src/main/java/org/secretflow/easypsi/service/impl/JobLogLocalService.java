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
package org.secretflow.easypsi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.secretflow.easypsi.common.util.FileUtils;
import org.secretflow.easypsi.manager.integration.job.JobManager;
import org.secretflow.easypsi.service.EnvService;
import org.secretflow.easypsi.service.JobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author beiwei
 * @date 2024/03/01
 */
@Service
//@ConditionalOnProperty(name = "easypsi.job-log.type", havingValue = "sls", matchIfMissing = true)
@Slf4j
public class JobLogLocalService implements JobLogService {
    @Autowired
    private EnvService envService;

    @Override
    public List<String> queryJobLog(String jobId) {
        return getProjectJobLogs(jobId);
    }

    private List<String> getProjectJobLogs(String jobId) {
        String directoryPath = JobManager.PROJECT_JOB_LOGS;
        log.info("get project job logs check job id: {}", jobId);
        FileUtils.fileNameCheck(jobId);
        String pattern = envService.getPlatformNodeId() + "_" + jobId + "-0";
        File directory = new File(directoryPath);
        FilenameFilter filenameFilter = (dir, name) -> name.startsWith(pattern);
        File[] matchingFiles = directory.listFiles(filenameFilter);
        String fileName = null;
        List<String> projectJobLogs = new ArrayList<>();
        long lastModified = 0L;
        if (matchingFiles != null) {
            for (File file : matchingFiles) {
                if (file.lastModified() > lastModified) {
                    fileName = file.getName();
                    lastModified = file.lastModified();
                }
            }
        }
        List<String> filePaths = FileUtils.traverseDirectories(new File(directoryPath + File.separator + fileName + File.separator + "secretflow"), ".log", FileUtils.FILE_PATH);
        if (filePaths == null) {
            log.warn("{} task log path not exit", jobId);
            return Collections.emptyList();
        }
        try {
            for (String filePath : filePaths) {
                projectJobLogs.addAll(Files.lines(Paths.get(filePath)).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("{} task log not exit", jobId);
            return Collections.emptyList();
        }

        if (ObjectUtils.isEmpty(fileName)) {
            log.warn("{} task log not exit", jobId);
            return Collections.emptyList();
        }
        return projectJobLogs;
    }

}
