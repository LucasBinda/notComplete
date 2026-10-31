package br.com.watchmatch.catalog.domain;

import java.util.List;

// Mesmo formato do TitleList em contracts/catalog-service.yaml
public record TitleList(List<TitleSummary> items, Boolean hasMore) {
}
