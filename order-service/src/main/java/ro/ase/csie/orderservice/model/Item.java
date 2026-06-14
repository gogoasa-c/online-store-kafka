package ro.ase.csie.orderservice.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Value;

@Value
@XmlAccessorType(XmlAccessType.FIELD)
public class Item {
    String sku;
    int qty;
    double price;
}
