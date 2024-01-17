// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.rag;

import com.microsoft.openai.samples.rag.config.SemanticKernelConfiguration;
import com.microsoft.semantickernel.Kernel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ContextConfiguration(classes = {SemanticKernelConfiguration.class})
@ExtendWith(SpringExtension.class)
class SemanticKernelTest {


    @Autowired
    Kernel kernel;

    @Test
    void test() {
        Assertions.assertNotNull(kernel);
    }

}
