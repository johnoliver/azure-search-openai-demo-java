// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.rag.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticKernelConfiguration {

    @Value("${openai.service}")
    String openAIServiceName;

    @Bean
    public TokenCredential localTokenCredential() {
        return new AzureCliCredentialBuilder().build();
    }

    @Bean
    public OpenAIAsyncClient defaultAsyncClient(TokenCredential tokenCredential) {
        String endpoint = "https://%s.openai.azure.com".formatted(openAIServiceName);
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(tokenCredential)
                .buildAsyncClient();
    }

    @Bean
    public Kernel skKernel(OpenAIAsyncClient client) {
        return SKBuilders.kernel()
                .withDefaultAIService(
                        SKBuilders.chatCompletion()
                                .withModelId("a-model")
                                .withOpenAIClient(client)
                                .build())
                .build();
    }
}
