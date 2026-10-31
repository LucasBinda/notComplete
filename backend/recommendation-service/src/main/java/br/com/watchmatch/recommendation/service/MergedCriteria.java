package br.com.watchmatch.recommendation.service;

import java.util.List;
import java.util.Map;

import br.com.watchmatch.recommendation.model.Criteria;

// Resultado da mistura: os critérios finais, as plataformas e de onde veio cada critério
public record MergedCriteria(Criteria criteria, List<Integer> providers, Map<String, String> sources) {
}
