package pcd.ass01;

import pcd.ass01.utility.SyncRunning;
import pcd.ass01.utility.SyncSuspension;

import java.util.Optional;

public abstract class BoidsSimulator {
    protected static final int FRAMERATE = 25;
    protected static final int N_THREAD = Runtime.getRuntime().availableProcessors() + 1;
    protected final BoidsModel model;
    protected final SyncRunning runningMonitor;
    protected final SyncSuspension suspensionMonitor;
    protected Optional<BoidsView> view;
    protected int framerate;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        this.runningMonitor = new SyncRunning();
        this.suspensionMonitor = new SyncSuspension();
        this.view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public BoidsModel getModel() { return model; }

    public abstract void startSimulation(int numBoids);
    public abstract void toggleSuspendResume();
    public abstract void stopSimulation();
}
