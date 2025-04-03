package pcd.ass01;

import java.util.concurrent.Callable;

public class UpdatePositionTask implements Callable<Void> {

    private final BoidsModel model;
    private final Boid boid;

    public UpdatePositionTask(final BoidsModel model, final Boid boid) {
        this.model = model;
        this.boid = boid;
    }

    @Override
    public Void call() throws Exception {
        this.boid.updatePos(model);
        return null;
    }
}