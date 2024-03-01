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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.data.DataSourceVO;
import org.secretflow.secretpad.service.model.data.DataTableInformationVo;
import org.secretflow.secretpad.service.model.data.DataVersionVO;
import org.secretflow.secretpad.service.model.data.GetDataTableInformatinoRequest;

/**
 * Data service interface
 *
 * @author xiaonan
 * @date 2023/6/7
 */
public interface DataService {

    /**
     * query csv data path
     *
     * @return
     */
    DataSourceVO queryDataPath();

    /**
     * query data version
     *
     * @return
     */
    DataVersionVO queryDataVersion();

    /**
     * query data table information
     *
     * @return
     */
    DataTableInformationVo queryDataTableInformation(GetDataTableInformatinoRequest request);

    DataTableInformationVo.DataTableInformation getDataTabelInformation(GetDataTableInformatinoRequest request);
}
