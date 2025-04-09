package pcd.ass01;

import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;
import pcd.ass01.BoidsView;
import pcd.ass01.task.PositionUpdateTask;
import pcd.ass01.task.VelocityUpdateTask;
import pcd.ass01.utility.SyncSuspension;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class MidTaskBoids {
    private static final int N_THREAD = Runtime.getRuntime().availableProcessors() + 1;
    private BoidsModel model;
    private Optional<BoidsView> view;
    private static final int FRAMERATE = 25;
    private int framerate;

    private int nCores = Runtime.getRuntime().availableProcessors() + 1;

    private Semaphore start;
    private SyncSuspension suspensionMonitor;
    private ExecutorService executor;

    private int nBoids;

    private boolean isRunning;
    private boolean isStopped;

    public MidTaskBoids(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        this.start = new Semaphore(0);
        this.suspensionMonitor = new SyncSuspension();
        this.suspensionMonitor.active();
    }

    public BoidsModel getModel(){
        return this.model;
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void startSimulation(int nBoids){
        this.nBoids = nBoids;
        try {
            this.start.release();
            this.start.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){
        while(true){
            try {
                this.start.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.isRunning = true;
            this.model.createSimulation(this.nBoids);
            this.start.release();
            this.isStopped = false;
            executor = Executors.newFixedThreadPool(N_THREAD);
            this.runSimulation();
        }
    }

    public void runSimulation() {
        while (!isStopped) {
            var t0 = System.currentTimeMillis();
            //suspensionMonitor.suspensionUntilResume();
            stateUpdate();
            GUIUpdate(t0);
        }
    }

    public void stateUpdate() {
        try {
            var boids = model.getBoids();

            final List<Future<Void>> resultsUpdateVelocity = new LinkedList<>();

            for (Boid boid : boids) {
                final Future<Void> res = executor.submit(new VelocityUpdateTask(model,boid));
                resultsUpdateVelocity.add(res);
            }

            for (Future<Void> updateVelocity : resultsUpdateVelocity) {
                updateVelocity.get();
            }

            final List<Future<Void>> resultsUpdatePosition = new LinkedList<>();

            for (Boid boid : boids) {
                final Future<Void> res = executor.submit(new PositionUpdateTask(model, boid));
                resultsUpdatePosition.add(res);
            }

            for (Future<Void> updatePos : resultsUpdatePosition) {
                updatePos.get();
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void GUIUpdate(final long t0) {
        if (view.isPresent()) {
            view.get().update(framerate);
            var t1 = System.currentTimeMillis();
            var dtElapsed = t1 - t0;
            var framePeriod = 1000 / FRAMERATE;

            if (dtElapsed < framePeriod) {
                try {
                    Thread.sleep(framePeriod - dtElapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                framerate = FRAMERATE;
            } else {
                framerate = (int) (1000 / dtElapsed);
            }
        }
    }

    public void toggleSuspendResume() {
        this.suspensionMonitor.resumeIfSuspended();
    }

    public void stopSimulation() {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (view.isPresent()) {
            view.get().resetToInitialScreen();
        }
    }

    public void setSeparationWeight(double value) {
        this.model.setSeparationWeight(value);
    }

    public void setAlignmentWeight(double value) {
        this.model.setAlignmentWeight(value);
    }

    public void setCohesionWeight(double value) {
        this.model.setCohesionWeight(value);
    }
}
