package pcd.ass01.task;

import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;

import java.util.concurrent.Callable;

public abstract class GenericBoidTask implements Callable<Void> {
    protected final BoidsModel model;
    protected final Boid boid;

    public GenericBoidTask(final BoidsModel model, final Boid boid) {
        this.model = model;
        this.boid = boid;
    }
}
