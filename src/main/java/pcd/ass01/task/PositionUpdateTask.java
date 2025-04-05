package pcd.ass01.task;

import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;

public class PositionUpdateTask extends GenericBoidTask {

    public PositionUpdateTask(final BoidsModel model, final Boid boid) {
        super(model, boid);
    }

    @Override
    public Void call() throws Exception {
        boid.updatePos(model);
        return null;
    }
}
