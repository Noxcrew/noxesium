package com.noxcrew.noxesium.api.feature.qib;

import com.noxcrew.noxesium.api.util.VectorAxis;
import org.joml.Vector3f;

/**
 * Defines various example qib effects that mimic vanilla behavior.
 */
public class ExampleQibEffects {
    /**
     * A qib definition with the same effect as the Lunge I spear.
     */
    public final QibDefinition LUNGE = QibDefinition.builder()
            .onAttack(new QibEffect.Conditional(
                    QibCondition.IS_IN_VEHICLE,
                    false,
                    new QibEffect.Conditional(
                            QibCondition.IS_GLIDING,
                            false,
                            new QibEffect.Conditional(
                                    QibCondition.IS_IN_WATER,
                                    false,
                                    new QibEffect.ApplyImpulse(
                                            new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.458f, 0.0f, 0.458f))))))
            .build();

    /**
     * A qib definition with the same effects as a potent sulfur geyser.
     */
    public final QibDefinition GEYSER = QibDefinition.builder()
            .whileInside(new QibEffect.Conditional(
                    QibCondition.IS_IN_VEHICLE,
                    false,
                    new QibEffect.Conditional(
                            QibCondition.IS_FLYING,
                            false,
                            new QibEffect.ConditionalMomentum(
                                    VectorAxis.Y, QibRelative.LESSER, 0.3f, new QibEffect.AddVelocity(0.0, 0.2, 0.0)))))
            .build();
}
