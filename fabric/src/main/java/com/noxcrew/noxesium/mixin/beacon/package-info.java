/**
 * Rewrites vanilla's way of rendering beacons. In vanilla every beacon block entity has its own draw call that
 * switches the buffer twice, once to render the non-translucent part of the beacon beam and once to render the
 * translucent part. It even does these two switches for every beam segment of the beacon beam. The change is to
 * batch up all these calls by doing only two buffer switches in total, rendering all beams and beam segments
 * for all beacons currently visible to the player at once.
 */
package com.noxcrew.noxesium.mixin.beacon;