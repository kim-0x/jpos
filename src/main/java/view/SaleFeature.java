package view;

import com.jpos.sale.model.SaleTransaction;

public interface SaleFeature {
    abstract void setProductPrice();
    abstract void getCurrentProductPrice();
    abstract void processSaleTransaction();
}
