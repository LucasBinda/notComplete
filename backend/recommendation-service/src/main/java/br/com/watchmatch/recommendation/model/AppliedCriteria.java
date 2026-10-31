package br.com.watchmatch.recommendation.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

// Critérios usados de verdade (filtros + IA). @JsonUnwrapped põe os campos de Criteria
// no mesmo nível do JSON, como pede o contrato.
// sources diz de onde veio cada critério: "filter" (você pediu) ou "ai" (a IA entendeu)
public record AppliedCriteria(
        @JsonUnwrapped Criteria criteria,
        List<Integer> providers,
        String referenceTitle,
        String summary,
        Map<String, String> sources) {
}
