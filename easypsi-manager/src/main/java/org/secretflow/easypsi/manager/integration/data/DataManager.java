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
package org.secretflow.easypsi.manager.integration.data;

import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.easypsi.common.errorcode.DataErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Future;

/**
 * @author liujunhao
 * @date 2024/01/30
 */
@Service
public class DataManager extends AbstractDataManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    @Override
    @Async("asyncExecutor")
    public Future<String> countLinesByCommand(String filePath, String fileName) {
        BufferedReader reader = null;
        try {
            Process process = new ProcessBuilder("awk", "NR == 100000001 { print \"100000001\"; exit } END { if (NR < 100000001) print NR } ", filePath).start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return new AsyncResult<>(reader.readLine());

        } catch (IOException e) {
            LOGGER.error("data table read error, data table: {}", fileName);
            throw EasyPsiException.of(DataErrorCode.QUERY_DATA_ERROR, fileName);
        } finally {
            if (!ObjectUtils.isEmpty(reader)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Close stream error", e);
                }
            }
        }
    }
}
