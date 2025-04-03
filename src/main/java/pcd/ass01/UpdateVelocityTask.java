package pcd.ass01;

import java.util.concurrent.Callable;

public class UpdateVelocityTask implements Callable<Void> {

    private final BoidsModel model;
    private final Boid boid;

    public UpdateVelocityTask(final BoidsModel model, final Boid boid) {
        this.model = model;
        this.boid = boid;
    }

    @Override
    public Void call() throws Exception {
        this.boid.updateVelocity(model);
        return null;
    }
}
