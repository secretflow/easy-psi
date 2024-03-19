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

package org.secretflow.easypsi.api;

import org.secretflow.easypsi.common.util.CertUtils;
import org.secretflow.easypsi.common.util.FileUtils;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.secretflow.v1alpha1.factory.KusciaAPIChannelFactory;
import org.secretflow.v1alpha1.factory.TlsConfig;
import org.secretflow.v1alpha1.interceptor.TokenAuthServerInterceptor;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * Domain Service Tests
 *
 * @author yansi
 * @date 2023/4/28
 */
public class DomainServiceTests {
    private final Logger LOGGER = LoggerFactory.getLogger(DomainServiceTests.class);
    private DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub;
    private String host;
    private int serverPort;
    private Server server;

    @BeforeEach
    public void setUp() throws Exception {
        initCerts();
        host = "localhost";
        serverPort = 8083;
        // start server
        startServer();
        // init client
        initStub();
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void initCerts() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("./target/test-classes/scripts/init_kusciaapi_certs.sh");
        Process process = pb.start();
        process.waitFor();
    }

    @Test
    public void testCreate() {
        Domain.CreateDomainRequest request = Domain.CreateDomainRequest.newBuilder().setDomainId("test").build();
        Domain.CreateDomainResponse response = domainServiceStub.createDomain(request);
        LOGGER.info(response.toString());
    }

    private void initStub() {
        String address = String.format("%s:%s", host, serverPort);
        String tokenFile = "classpath:certs/token";
        TlsConfig tlsConfig = new TlsConfig();
        tlsConfig.setCertFile("classpath:certs/client.crt");
        tlsConfig.setKeyFile("classpath:certs/client.pem");
        tlsConfig.setCaFile("classpath:certs/ca.crt");

        KusciaAPIChannelFactory channelFactory = new KusciaAPIChannelFactory(address, tokenFile, tlsConfig,"mtls");
        domainServiceStub = DomainServiceGrpc.newBlockingStub(channelFactory.newClientChannel());
    }

    // create & start a server.
    private void startServer() throws Exception {
        File serverCertFile = FileUtils.readFile("classpath:certs/server.crt");
        File serverPrivateKeyFile = FileUtils.readFile("classpath:certs/server.pem");
        X509Certificate[] serverTrustedCaCerts = {CertUtils.loadX509Cert("classpath:certs/ca.crt")};
        String token = FileUtils.readFile2String("classpath:certs/token");
        server = serverBuilder(serverPort, serverCertFile, serverPrivateKeyFile, serverTrustedCaCerts)
                .intercept(new TokenAuthServerInterceptor(token))
                .addService(new DomainServiceImpl())
                .build()
                .start();
    }

    private ServerBuilder<?> serverBuilder(int port, File serverCertFile, File serverKeyFile, X509Certificate[] serverTrustedCaCerts)
            throws IOException {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(serverCertFile, serverKeyFile);
        GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL);
        sslContextBuilder.trustManager(serverTrustedCaCerts).clientAuth(ClientAuth.REQUIRE);

        return NettyServerBuilder.forPort(port).sslContext(sslContextBuilder.build());
    }

    private class DomainServiceImpl extends DomainServiceGrpc.DomainServiceImplBase {

        @Override
        public void createDomain(Domain.CreateDomainRequest request, StreamObserver<Domain.CreateDomainResponse> responseObserver) {
            responseObserver.onNext(Domain.CreateDomainResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void queryDomain(Domain.QueryDomainRequest request, StreamObserver<Domain.QueryDomainResponse> responseObserver) {
            responseObserver.onNext(Domain.QueryDomainResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void updateDomain(Domain.UpdateDomainRequest request, StreamObserver<Domain.UpdateDomainResponse> responseObserver) {
            responseObserver.onNext(Domain.UpdateDomainResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void deleteDomain(Domain.DeleteDomainRequest request, StreamObserver<Domain.DeleteDomainResponse> responseObserver) {
            responseObserver.onNext(Domain.DeleteDomainResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void batchQueryDomainStatus(Domain.BatchQueryDomainStatusRequest request,
                                           StreamObserver<Domain.BatchQueryDomainStatusResponse> responseObserver) {
            responseObserver.onNext(Domain.BatchQueryDomainStatusResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }
}
