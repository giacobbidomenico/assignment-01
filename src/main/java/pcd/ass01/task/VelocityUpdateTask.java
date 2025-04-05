package pcd.ass01.task;

import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;

public class VelocityUpdateTask extends GenericBoidTask {

    public VelocityUpdateTask(final BoidsModel model, final Boid boid) {
        super(model, boid);
    }

    @Override
    public Void call() throws Exception {
        boid.updateVelocity(model);
        return null;
    }
}
