package com.example.orderservice.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Value;

@Value
@XmlAccessorType(XmlAccessType.FIELD)
public class ShippingAddress {
    String street;
    String city;
    String zip;
}
