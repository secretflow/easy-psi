/*
 *   Copyright 2023 Ant Group Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.secretflow.easypsi.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Index controller test
 *
 * @author lihaixin
 * @date 2023/12/14
 */
class IndexControllerTest extends ControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @Test
    void indexMapping() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));


        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(MockMvcRequestBuilders.get("/guide"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(MockMvcRequestBuilders.get("/task"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(MockMvcRequestBuilders.get("/task-details"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(MockMvcRequestBuilders.get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(MockMvcRequestBuilders.get("/auth"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}