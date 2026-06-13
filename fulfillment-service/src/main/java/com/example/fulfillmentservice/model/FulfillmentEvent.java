package com.example.fulfillmentservice.model;

import com.example.fulfillmentservice.model.adapter.InstantAdapter;
import com.example.fulfillmentservice.model.adapter.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.Instant;
import java.time.LocalDate;

@XmlRootElement(name = "fulfillmentEvent")
@XmlAccessorType(XmlAccessType.FIELD)
public class FulfillmentEvent {

    private String orderId;
    private String customerEmail;
    private String warehouseId;

    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant dispatchTimestamp;

    private String trackingCode;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate estimatedDelivery;

    public FulfillmentEvent() {}

    public FulfillmentEvent(String orderId, String customerEmail, String warehouseId,
                            Instant dispatchTimestamp, String trackingCode, LocalDate estimatedDelivery) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.warehouseId = warehouseId;
        this.dispatchTimestamp = dispatchTimestamp;
        this.trackingCode = trackingCode;
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public Instant getDispatchTimestamp() { return dispatchTimestamp; }
    public void setDispatchTimestamp(Instant dispatchTimestamp) { this.dispatchTimestamp = dispatchTimestamp; }

    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }

    public LocalDate getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDate estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
}
