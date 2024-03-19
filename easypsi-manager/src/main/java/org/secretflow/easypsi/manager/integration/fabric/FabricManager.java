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

package org.secretflow.easypsi.manager.integration.fabric;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.client.*;
import org.hyperledger.fabric.client.identity.*;
import org.secretflow.easypsi.manager.properties.FabricAPIProperties;
import org.secretflow.easypsi.persistence.entity.FabricLogDO;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manager fabric operation
 *
 * @author lihaixin
 * @date 2024/01/13
 */
@RequiredArgsConstructor
@Service
@Slf4j
@EnableConfigurationProperties({FabricAPIProperties.class})
public class FabricManager {


    public String READ_ASSET = "ReadAsset";


    private final FabricAPIProperties fabricAPIProperties;


    @PostConstruct
    public void validateFile() {
        try {
            if (fabricAPIProperties.getIsOpen()) {
                newContract();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }


    /**
     * Get configuration open status
     *
     * @return {@link Boolean }
     */

    public Boolean isOpen() {
        return fabricAPIProperties.getIsOpen();
    }


    /**
     * Evaluate transaction by asset id
     *
     * @param assetId
     * @return {@link String }
     */

    public String evaluateTransactionByAssetId(String assetId) throws CertificateException, IOException, InvalidKeyException, GatewayException {
        Contract contract = newContract();
        return new String(contract.evaluateTransaction(READ_ASSET, assetId));
    }


    /**
     * Submit transaction
     *
     * @param fabricLogDO
     */

    public void submitTransaction(FabricLogDO fabricLogDO) throws CertificateException, IOException, InvalidKeyException, EndorseException, CommitException, SubmitException, CommitStatusException {
        fabricLogDO.setChannelName(fabricAPIProperties.getChannelName());
        fabricLogDO.setChainCodeName(fabricAPIProperties.getChainCodeName());
        fabricLogDO.setMspId(fabricAPIProperties.getMspId());
        fabricLogDO.setOverrideAuth(fabricAPIProperties.getOverrideAuth());
        Contract contract = newContract();
        if (StringUtils.isBlank(fabricLogDO.getOwner())) {
            fabricLogDO.setOwner(fabricAPIProperties.getOwner());
        }
        fabricLogDO.setOwner(fabricAPIProperties.getOwner());
        contract.submitTransaction("CreateAsset", fabricLogDO.getLogHash(), fabricLogDO.getOwner(), fabricLogDO.getLogPath());
    }


    /**
     * Create a new contract
     *
     * @return {@link Contract }
     */

    public Contract newContract() throws IOException, CertificateException, InvalidKeyException {
        ManagedChannel channel = newChannel();
        Gateway.Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
                // Default timeouts for different gRPC calls
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS)).endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS)).submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS)).commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));
        return builder.connect().getNetwork(fabricAPIProperties.getChannelName()).getContract(fabricAPIProperties.getChainCodeName());
    }


    public ManagedChannel newChannel() throws IOException {
        return Grpc.newChannelBuilder(fabricAPIProperties.getAddress(), TlsChannelCredentials.newBuilder().trustManager(new File(fabricAPIProperties.getTlsCertPath())).build()).overrideAuthority(fabricAPIProperties.getOverrideAuth()).build();
    }

    /**
     * New identity
     *
     * @return {@link Identity }
     */

    private Identity newIdentity() throws IOException, CertificateException {
        var certReader = Files.newBufferedReader(Path.of(fabricAPIProperties.getSignCertPath()));
        var certificate = Identities.readX509Certificate(certReader);
        return new X509Identity(fabricAPIProperties.getMspId(), certificate);
    }

    /**
     * New signer
     *
     * @return {@link Signer }
     */

    private Signer newSigner() throws IOException, InvalidKeyException {
        var keyReader = Files.newBufferedReader(Path.of(fabricAPIProperties.getKeystorePath()));
        var privateKey = Identities.readPrivateKey(keyReader);
        return Signers.newPrivateKeySigner(privateKey);
    }

    @Bean("fabricThreadPool")
    public ThreadPoolTaskExecutor fabricThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(11);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setThreadNamePrefix("fabricThreadPool-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}