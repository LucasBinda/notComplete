package br.com.watchmatch.catalog.domain;

// offer: flatrate (assinatura), free, ads (grátis com anúncios), rent (aluguel) ou buy (compra)
public record WatchProvider(Integer id, String name, String logoUrl, String offer, String watchUrl) {
}
