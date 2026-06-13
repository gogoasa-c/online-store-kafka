package com.example.fulfillmentservice.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Slim warehouse dispatch document produced by order-to-fulfillment.xsl.
 * Contains only the fields the warehouse needs: orderId, items, shippingAddress.
 */
@XmlRootElement(name = "fulfillmentWorkItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class FulfillmentWorkItem {

    private String orderId;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    private ShippingAddress shippingAddress;

    public FulfillmentWorkItem() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }
}
