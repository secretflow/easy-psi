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

package org.secretflow.easypsi.manager.integration.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data Manager Test
 *
 * @author lihaixin
 * @date 2024/03/08
 */
@ExtendWith(MockitoExtension.class)
public class DataManagerTest {


    @InjectMocks
    private DataManager dataManager;

    @TempDir
    Path tempDir;

    private static final String FILE_CONTENT = "Hello, World!";


    @Test
    public void countLinesByCommandTest() throws IOException {
        Path path = Files.writeString(tempDir.resolve("test.txt"), FILE_CONTENT);
        dataManager.countLinesByCommand(path.toFile().getParentFile().getPath(), path.getFileName().toString());
    }

}