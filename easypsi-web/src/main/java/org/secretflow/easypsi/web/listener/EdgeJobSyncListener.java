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

package org.secretflow.easypsi.web.listener;

import org.secretflow.easypsi.common.constant.DomainRouterConstants;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.common.util.RestTemplateUtil;
import org.secretflow.easypsi.persistence.entity.NodeRouteDO;
import org.secretflow.easypsi.persistence.repository.NodeRouteRepository;
import org.secretflow.easypsi.service.EnvService;
import org.secretflow.easypsi.service.NodeRouterService;
import org.secretflow.easypsi.service.ProjectService;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.service.model.project.ProjectJobVO;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;

/**
 * Edge job tasks synchronized listener
 *
 * @author xujiening
 * @date 2023/11/1
 */
@Component
@ConditionalOnProperty(name = "flow.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class EdgeJobSyncListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeJobSyncListener.class);

    /**
     * domain route cache, clear 30 seconds
     * <domainRouteId, nodeRouteStatus>
     */
    private final Cache<String, String> domainRouteCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofSeconds(30)).build();

    @Autowired
    private ProjectService projectService;

    @Autowired
    private NodeRouteRepository nodeRouteRepository;

    @Autowired
    private NodeRouterService nodeRouterService;

    @Autowired
    private EnvService envService;

    /**
     * "http://localhost:8080/api/v1alpha1/user/node";
     */
    @Value("${easypsi.gateway}")
    private String gateway;

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void onSyncEdgeJobs() {
        // todo: use backoff
        try {
            LOGGER.info("sync edge job list run start");
            // get edge node id
            String platformNodeId = envService.getPlatformNodeId();
            // list node route list
            List<NodeRouteDO> nodeRouterDOList = nodeRouteRepository.findBySrcNodeId(platformNodeId);
            if (CollectionUtils.isEmpty(nodeRouterDOList)) {
                return;
            }
            List<String> syncNodeIdList = nodeRouterDOList.stream().map(NodeRouteDO::getDstNodeId).toList();
            syncNodeIdList.forEach(requestNodeId -> {
                String nodeRouteStatus = "";
                // check cache first
                String cacheKey = platformNodeId + "_" + requestNodeId;
                if (domainRouteCache.getIfPresent(cacheKey) != null) {
                    nodeRouteStatus = domainRouteCache.getIfPresent(cacheKey);
                } else {
                    nodeRouteStatus = nodeRouterService.getNodeRouteStatus(platformNodeId, requestNodeId);
                    domainRouteCache.put(cacheKey, StringUtils.isBlank(nodeRouteStatus) ? "" : nodeRouteStatus);
                }
                // check node route status
                if (StringUtils.isBlank(nodeRouteStatus) || !StringUtils.equals(nodeRouteStatus, DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name())) {
                    return;
                }
                EasyPsiResponse secretPadResponse = RestTemplateUtil.sendPostJson(queryEdgeProjectUrl() + "/edge/job/list",
                        ImmutableMap.of(), buildHeader(requestNodeId), EasyPsiResponse.class);
                Object responseData = secretPadResponse.getData();
                // project job list
                List<ProjectJobVO> projectJobVOList = JsonUtils.toJavaList(JsonUtils.toJSONString(responseData), ProjectJobVO.class);
                // sync status
                projectService.syncHostNodeProjectJobs(projectJobVOList);
            });
        } catch (Exception ex) {
            LOGGER.error("sync edge job list run error: {}", ex);
        }
    }

    private final String HTTP_HEADER = "http://";

    private String queryEdgeProjectUrl() {
        return HTTP_HEADER + gateway + "/api/v1alpha1/project";
    }

    @NotNull
    private ImmutableMap<String, String> buildHeader(String requestId) {
        String svc = "secretpad." + requestId + ".svc";
        return ImmutableMap.of("Host", svc);
        // TODO 调试使用 "Host", centerUserNodeUrl(),
        // return ImmutableMap.of("Content-Type", "application/json", "kuscia-origin-source", requestId);
    }
}