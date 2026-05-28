package com.jpos.sale.model;

import java.util.UUID;

public record ProductRef(UUID productId, String barcode) {
}