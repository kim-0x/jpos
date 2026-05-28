package com.jpos.sale.model;

import java.util.UUID;

public record ProductInfo(UUID productId, String barcode, String name, double cost) {
}