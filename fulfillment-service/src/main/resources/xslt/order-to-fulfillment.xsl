<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <!--
        Transforms an <orderRequest> into a slim <fulfillmentWorkItem>.
        Retains only the fields the warehouse needs: orderId, items, shippingAddress.
        Drops: customerId, customerEmail, paymentStatus, timestamp.
    -->
    <xsl:template match="/">
        <fulfillmentWorkItem>
            <orderId>
                <xsl:value-of select="/orderRequest/orderId"/>
            </orderId>
            <items>
                <xsl:for-each select="/orderRequest/items/item">
                    <item>
                        <sku><xsl:value-of select="sku"/></sku>
                        <qty><xsl:value-of select="qty"/></qty>
                        <price><xsl:value-of select="price"/></price>
                    </item>
                </xsl:for-each>
            </items>
            <shippingAddress>
                <street><xsl:value-of select="/orderRequest/shippingAddress/street"/></street>
                <city><xsl:value-of select="/orderRequest/shippingAddress/city"/></city>
                <zip><xsl:value-of select="/orderRequest/shippingAddress/zip"/></zip>
            </shippingAddress>
        </fulfillmentWorkItem>
    </xsl:template>

</xsl:stylesheet>
