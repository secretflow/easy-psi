package org.secretflow.secretpad.web.init;

import org.secretflow.secretpad.service.NodeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


/**
 * @author chixian
 * @date 2023/10/27
 */
@Profile(value = {"p2p"})
@Service
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private NodeService nodeService;

    @Override
    public void run(String... args) {
        try {
            nodeService.initialNode();
        }catch (Exception e){
            log.error("node init error :",e);
        }
    }
}
