package com.intelcia.myITAssist.service.provider;

import com.intelcia.myITAssist.model.AiModel;
import com.intelcia.myITAssist.model.Provider;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Strategy interface for building a Spring AI {@link ChatModel} from a persisted {@link AiModel}.
 * Add a new implementation + {@code @Component} to support a new provider — no other changes needed.
 */
public interface AiProvider {

    /** Returns true if this implementation handles the given provider type. */
    boolean supports(Provider provider);

    /** Builds a ready-to-use {@link ChatModel} from the given configuration. */
    ChatModel build(AiModel model);
}
