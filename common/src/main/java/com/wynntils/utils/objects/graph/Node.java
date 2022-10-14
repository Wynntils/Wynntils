/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.graph;

import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.Referencable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

/**
 * A mutable node to be used in a graph structure.
 *
 * @author Kepler-17c
 */
class Node implements IBoundingBox, Referencable {
    /**
     * Serialisation ID.
     */
    private final UUID uuid;
    /**
     * Helper variable for {@link IBoundingBox}.
     */
    private final AxisAlignedBoundingBox bounds;
    /**
     * Paths connected to this node.
     */
    final List<Path> connectedPaths;
    /**
     * Position of the node in space.
     */
    private Vec3 position;
    /**
     * Whether this node has been fully processed in creating the graph.
     */
    private boolean closed;

    /**
     * Deserialisation constructor with given ID.
     *
     * @param position
     *            of the node.
     * @param closed
     *            status of graph generation.
     * @param uuid
     *            for serialisation.
     */
    public Node(final Vec3 position, final boolean closed, final UUID uuid) {
        this.uuid = uuid;
        this.bounds = new AxisAlignedBoundingBox(position);
        this.connectedPaths = new ArrayList<>();
        this.position = position;
        this.closed = closed;
    }

    /**
     * Base constructor for a new node.
     * <p>
     * It's state is set to closed and a random UUID is generated.
     * </p>
     *
     * @param position
     *            of the node.
     */
    public Node(final Vec3 position) {
        this(position, false, UUID.randomUUID());
    }

    /**
     * Gets the number of connected paths registered in this node.
     *
     * @return The number of connected nodes.
     */
    public int degree() {
        return this.connectedPaths.size();
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Gets the processing status from graph generation.
     *
     * @return The state of this node.
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Closes the node.
     */
    public void close() {
        this.closed = true;
    }

    @Override
    public AxisAlignedBoundingBox getBounds() {
        return this.bounds;
    }
}
