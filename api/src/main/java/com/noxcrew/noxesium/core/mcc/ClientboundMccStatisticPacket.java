package com.noxcrew.noxesium.core.mcc;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by MCC Island whenever the player obtains a statistic key.
 *
 * @param statistic The identifier of the statistic that was incremented.
 * @param record    If true, this is a record type statistic where the new value represents the new record.
 * @param value     The amount the statistic incremented by.
 */
public record ClientboundMccStatisticPacket(String statistic, boolean record, int value) implements NoxesiumPacket {}
