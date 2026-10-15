package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.chat.ParsedOrder;

/** Orden activa dada por un jugador: qué pidió, quién la dio y en qué estado está. */
public final class OrderState {
    public enum Status { ACTIVE, FINISHED, FAILED }

    private final ParsedOrder order;
    private final String issuer;
    private Status status = Status.ACTIVE;
    private String failure = "";

    public OrderState(ParsedOrder order, String issuer) {
        this.order = order;
        this.issuer = issuer;
    }

    public ParsedOrder order() {
        return order;
    }

    public String issuer() {
        return issuer;
    }

    public Status status() {
        return status;
    }

    /** Motivo del fallo, para responder en el chat. Vacío mientras la orden no ha fallado. */
    public String failure() {
        return failure;
    }

    /** La orden se cumplió o se canceló: el NPC vuelve a sus tareas autónomas. */
    public void finish() {
        if (status == Status.ACTIVE) {
            status = Status.FINISHED;
        }
    }

    /** La orden no se puede cumplir: no hay ruta o no encuentra el objetivo. */
    public void fail(String reason) {
        if (status == Status.ACTIVE) {
            status = Status.FAILED;
            failure = reason;
        }
    }

    /** El jugador al que va dirigida la orden no está conectado. */
    public void onTargetMissing(String playerName) {
        fail(playerName + " no está conectado");
    }
}
