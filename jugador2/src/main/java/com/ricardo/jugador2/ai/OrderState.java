package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.chat.ParsedOrder;

/** Orden activa dada por un jugador: qué pidió, quién la dio y en qué estado está. */
public final class OrderState {
    public enum Status { ACTIVE, FINISHED, FAILED }

    private final ParsedOrder order;
    private final String issuer;
    private Status status = Status.ACTIVE;
    private String failure = "";
    private String reply = "";

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

    /** La orden se cumplió o se canceló sin que el NPC tenga nada que decir. */
    public void finish() {
        finish("");
    }

    /** La orden se cumplió: el NPC vuelve a sus tareas autónomas y dice {@code said} en el chat. */
    public void finish(String said) {
        if (status == Status.ACTIVE) {
            status = Status.FINISHED;
            reply = said;
        }
    }

    /** La orden no se puede cumplir: no hay ruta o no encuentra el objetivo. El NPC lo dice en el chat. */
    public void fail(String reason) {
        if (status == Status.ACTIVE) {
            status = Status.FAILED;
            failure = reason;
            reply = "No puedo: " + reason;
        }
    }

    /** Lo que el NPC dice en el chat sobre esta orden. Se entrega una sola vez; vacío si no hay nada que decir. */
    public String takeReply() {
        String said = reply;
        reply = "";
        return said;
    }

    /** El jugador al que va dirigida la orden no está conectado. */
    public void onTargetMissing(String playerName) {
        fail(playerName + " no está conectado");
    }
}
