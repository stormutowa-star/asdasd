package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.chat.Come;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStateTest {
    @Test
    void activeUntilFinished() {
        assertEquals(OrderState.Status.ACTIVE, new OrderState(new Come(), "Steve").status());
    }

    @Test
    void stopFinishesOrder() {
        OrderState order = new OrderState(new Come(), "Steve");
        order.finish();
        assertEquals(OrderState.Status.FINISHED, order.status());
    }

    @Test
    void missingTargetFailsOrder() {
        OrderState order = new OrderState(new Come(), "Steve");
        order.onTargetMissing("Steve");
        assertEquals(OrderState.Status.FAILED, order.status());
    }
}
