package com.example.food_front.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para la respuesta de la API de preferencia de MercadoPago
 */
public class MercadoPagoPreference {

    @SerializedName("init_point")
    private String initPoint;

    @SerializedName("preference_id")
    private String preferenceId;

    @SerializedName("payment_request_id")
    private String paymentRequestId;

    public MercadoPagoPreference(String initPoint, String preferenceId, String paymentRequestId) {
        this.initPoint = initPoint;
        this.preferenceId = preferenceId;
        this.paymentRequestId = paymentRequestId;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }
}
