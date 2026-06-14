package com.tinderbot.telegram.service.questionnaire;

import com.tinderbot.telegram.model.QuestionnaireType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QuestionnaireStrategyRegistry {

    private final Map<QuestionnaireType, QuestionnaireStrategy> strategies;

    public QuestionnaireStrategyRegistry(List<QuestionnaireStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(QuestionnaireStrategy::type, Function.identity()));
    }

    public QuestionnaireStrategy require(QuestionnaireType type) {
        QuestionnaireStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Неизвестный тип опросника: " + type);
        }
        return strategy;
    }
}
