package com.lognex.api.model.entity;

import com.lognex.api.model.base.AbstractEntityLegendable;
import com.lognex.api.model.entity.good.AbstractGood;
import com.lognex.api.model.entity.good.Assortment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Consignment extends AbstractEntityLegendable implements Assortment {

    private String label;
    private List<Barcode> barcodes;
    private AbstractGood assortment;
}
