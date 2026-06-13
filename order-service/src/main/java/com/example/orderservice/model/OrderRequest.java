package com.example.orderservice.model;

import com.example.orderservice.model.adapter.InstantAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "orderRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderRequest {

    private String orderId;
    private String customerId;
    private String customerEmail;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    private ShippingAddress shippingAddress;
    private String paymentStatus;

    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant timestamp;
}
