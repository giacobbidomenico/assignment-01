package pcd.ass01;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BoidsSimulator {

    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;;

    private BoidsModel model;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private int framerate;

    private ExecutorService executor;
    
    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        this.view = Optional.empty();
        this.executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }
      
    public void runSimulation() {
        while (true) {
            try {
                var t0 = System.currentTimeMillis();
                var boids = model.getBoids();

                final List<Future<Void>> resultsUpdateVelocity = new LinkedList<>();

                for (Boid boid : boids) {
                    final Future<Void> res = executor.submit(new UpdateVelocityTask(model, boid));
                    resultsUpdateVelocity.add(res);
                }

                for (Future<Void> updateVelocity : resultsUpdateVelocity) {
                    updateVelocity.get();
                }

                final List<Future<Void>> resultsUpdatePosition= new LinkedList<>();

                for (Boid boid : boids) {
                    final Future<Void> res = executor.submit(new UpdatePositionTask(model, boid));
                    resultsUpdatePosition.add(res);
                }

                for (Future<Void> updatePos : resultsUpdatePosition) {
                    updatePos.get();
                }


                if (view.isPresent()) {
                    view.get().update(framerate);
                    var t1 = System.currentTimeMillis();
                    var dtElapsed = t1 - t0;
                    var framratePeriod = 1000 / FRAMERATE;

                    if (dtElapsed < framratePeriod) {
                        try {
                            Thread.sleep(framratePeriod - dtElapsed);
                        } catch (Exception ex) {
                        }
                        framerate = FRAMERATE;
                    } else {
                        framerate = (int) (1000 / dtElapsed);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
