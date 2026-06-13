package com.example.notificationservice.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@XmlRootElement(name = "fulfillmentEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class FulfillmentEvent {
    private String orderId;
    private String customerEmail;
    private String warehouseId;
    private String dispatchTimestamp;
    private String trackingCode;
    private String estimatedDelivery;
}
