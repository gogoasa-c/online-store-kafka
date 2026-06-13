<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <!--
        Transforms a <fulfillmentEvent> into a <notificationPayload> ready for email dispatch.
        recipientEmail  ← customerEmail
        subject         ← constructed from orderId
        orderSummary    ← constructed from orderId, warehouseId, dispatchTimestamp, estimatedDelivery
        trackingUrl     ← https://track.example.com/<trackingCode>
    -->
    <xsl:template match="/">
        <notificationPayload>
            <recipientEmail>
                <xsl:value-of select="/fulfillmentEvent/customerEmail"/>
            </recipientEmail>
            <subject>
                <xsl:value-of select="concat('Your order ', /fulfillmentEvent/orderId, ' has shipped!')"/>
            </subject>
            <orderSummary>
                <xsl:value-of select="concat(
                    'Order ', /fulfillmentEvent/orderId,
                    ' dispatched from ', /fulfillmentEvent/warehouseId,
                    ' on ', /fulfillmentEvent/dispatchTimestamp,
                    '. Estimated delivery: ', /fulfillmentEvent/estimatedDelivery, '.')"/>
            </orderSummary>
            <trackingUrl>
                <xsl:value-of select="concat('https://track.example.com/', /fulfillmentEvent/trackingCode)"/>
            </trackingUrl>
        </notificationPayload>
    </xsl:template>

</xsl:stylesheet>
