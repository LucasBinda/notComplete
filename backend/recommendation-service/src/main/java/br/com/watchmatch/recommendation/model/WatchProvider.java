package br.com.watchmatch.recommendation.model;

// offer: flatrate (assinatura), free, ads, rent (aluguel) ou buy (compra)
public record WatchProvider(Integer id, String name, String logoUrl, String offer, String watchUrl) {
}
