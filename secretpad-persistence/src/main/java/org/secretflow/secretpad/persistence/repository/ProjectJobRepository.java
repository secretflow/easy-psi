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

package org.secretflow.secretpad.persistence.repository;

import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Project job repository
 *
 * @author yansi
 * @date 2023/5/31
 */
@Repository
public interface ProjectJobRepository extends JpaRepository<ProjectJobDO, ProjectJobDO.UPK>, JpaSpecificationExecutor<ProjectJobDO> {

    /**
     * Query project job result by jobId
     *
     * @param jobId target jobId
     * @return project job result
     */
    @Query("from ProjectJobDO pj where pj.upk.jobId=:jobId")
    Optional<ProjectJobDO> findByJobId(@Param("jobId") String jobId);

    /**
     * Query project job result list by jobId list
     *
     * @param jobIds target jobId list
     * @return project job result list
     */
    @Query("from ProjectJobDO pj where pj.upk.jobId in :jobIds")
    List<ProjectJobDO> findByJobIds(@Param("jobIds") List<String> jobIds);

    /**
     * Paging query project job results by pageable
     *
     * @param pageable
     * @return
     */
    @Query("from ProjectJobDO pj")
    Page<ProjectJobDO> page(Pageable pageable);

    /**
     * Query project job results by nodeId
     *
     * @param nodeId
     * @return
     */
    @Query("from ProjectJobDO pj where pj.initiatorNodeId=:nodeId or pj.partnerNodeId=:nodeId")
    List<ProjectJobDO> findByNodeId(@Param("nodeId") String nodeId);

    @Query("update ProjectJobDO pj set pj.status='RUNNING', pj.startTime=:startTime where pj.upk.jobId=:jobId")
    @Modifying
    void startJob(@Param("jobId") String jobId, @Param("startTime") LocalDateTime startTime);


    /**
     * Query jobs by host nodeId
     *
     * @param hostNodeId    target hostNodeId
     * @param requestNodeId target requestNodeId
     * @return
     */
    @Query("from ProjectJobDO pj where pj.hostNodeId=:hostNodeId and (pj.initiatorNodeId=:requestNodeId or pj.partnerNodeId=:requestNodeId)")
    List<ProjectJobDO> queryJobsByHostNodeIdAndRequestNodeId(@Param("hostNodeId") String hostNodeId, @Param("requestNodeId") String requestNodeId);

    @Query("from ProjectJobDO pj where pj.status in :status and (pj.initiatorNodeId=:nodeId or pj.partnerNodeId=:nodeId)")
    List<ProjectJobDO> queryJobInStatus(List<String> status,@Param("nodeId") String nodeId);

}