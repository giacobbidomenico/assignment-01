package pcd.ass01;


import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;
import pcd.ass01.BoidsView;
import pcd.ass01.task.PositionUpdateTask;
import pcd.ass01.task.VelocityUpdateTask;
import pcd.ass01.utility.SyncSuspension;
import pcd.ass01.utility.SyncRunning;
import pcd.ass01.utility.SafeBoolean;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class TaskBoidSimulator {
    private static final int N_THREAD = Runtime.getRuntime().availableProcessors() + 1;
    private static final int FRAMERATE = 25;

    private BoidsModel model;
    private Barrier initialHandshackePoint;
    private Barrier finalHandshakePoint;

    private SyncRunning runningMonitor;
    private SyncSuspension suspensionMonitor;

    private ExecutorService executor;
    private int nBoids;
    private int framerate;
    private Optional<BoidsView> view;

    private final  SafeBoolean isStopped;

    public TaskBoidSimulator(BoidsModel model) {
        this.model = model;
        this.view = Optional.empty();
        this.initialHandshackePoint = new Barrier(2);
        this.finalHandshakePoint = new Barrier(2);
        this.runningMonitor = new SyncRunning();
        this.suspensionMonitor = new SyncSuspension();
        this.executor = Executors.newFixedThreadPool(N_THREAD);
        this.isStopped = new SafeBoolean();
    }

    public BoidsModel getModel(){
        return this.model;
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void startSimulation(int nBoids){
        this.executor = Executors.newCachedThreadPool();
        this.nBoids = nBoids;
        try {
            this.initialHandshackePoint.notifyJobDone();
            this.finalHandshakePoint.notifyJobDone();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){
        while(true){
            try {
                this.initialHandshackePoint.notifyJobDone();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.model.createSimulation(this.nBoids);
            try {
                this.finalHandshakePoint.notifyJobDone();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            this.isStopped.setFalse();
            this.runningMonitor.active();

            this.runSimulation();
        }
    }

    public void runSimulation() {
        while (!isStopped.get()) {
            var t0 = System.currentTimeMillis();
            suspensionMonitor.suspensionUntilResume();
            stateUpdate();
            GUIUpdate(t0);
            runningMonitor.waitIfStopped();
        }
    }

    public void stateUpdate() {
        try {
            var boids = model.getBoids();

            final List<Future<Void>> resultsUpdateVelocity = new LinkedList<>();

            for (Boid boid : boids) {
                if (!executor.isShutdown() && !executor.isTerminated()) {
                    final Future<Void> res = executor.submit(new VelocityUpdateTask(model,boid));
                    resultsUpdateVelocity.add(res);
                }
            }

            for (Future<Void> updateVelocity : resultsUpdateVelocity) {
                updateVelocity.get();
            }

            final List<Future<Void>> resultsUpdatePosition = new LinkedList<>();

            for (Boid boid : boids) {
                if (!executor.isShutdown() && !executor.isTerminated()) {
                    final Future<Void> res = executor.submit(new PositionUpdateTask(model, boid));
                    resultsUpdatePosition.add(res);
                }
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
        if (view.isPresent()) {
            final String s = view.get().getSuspendResumeButtonText();
            view.get().updateSuspendResumeButtonText(s.equals("Resume") ? "Suspend" : "Resume");
        }
        suspensionMonitor.changeState();
    }

    public void stopSimulation() {

        try {
            this.executor.shutdown();
            this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        runningMonitor.stop();
        runningMonitor.waitUntilArriveWait();

        isStopped.setTrue();

        if (view.isPresent()) {
            view.get().resetToInitialScreen();
        }
        runningMonitor.active();
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

