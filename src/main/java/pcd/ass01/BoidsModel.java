package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsModel {
    
    private final List<Boid> boids;
    private double separationWeight; 
    private double alignmentWeight; 
    private double cohesionWeight; 
    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;

    private final ReentrantLock separationLock = new ReentrantLock();
    private final ReentrantLock alignmentLock = new ReentrantLock();
    private final ReentrantLock cohesionLock = new ReentrantLock();

    private final double initialSeparationWeight; 
    private final double initialAlignmentWeight; 
    private final double initialCohesionWeight; 

    public BoidsModel( 
    						double initialSeparationWeight, 
    						double initialAlignmentWeight, 
    						double initialCohesionWeight,
    						double width, 
    						double height,
    						double maxSpeed,
    						double perceptionRadius,
    						double avoidRadius){
        this.initialSeparationWeight = initialSeparationWeight;
        this.initialAlignmentWeight = initialAlignmentWeight;
        this.initialCohesionWeight = initialCohesionWeight;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;
        boids = new ArrayList<>();
    }

    public List<Boid> getBoids() {
        return boids;
    }
    
    public double getMinX() {
        return -width/2;
    }

    public double getMaxX() {
        return width/2;
    }

    public double getMinY() {
        return -height/2;
    }

    public double getMaxY() {
        return height/2;
    }
    
    public double getWidth() {
        return width;
    }
 
    public double getHeight() {
        return height;
    }

    public void setSeparationWeight(double value) {
        separationLock.lock();
        try {
            this.separationWeight = value;
        } finally {
            separationLock.unlock();
        }
    }

    public void setAlignmentWeight(double value) {
        alignmentLock.lock();
        try {
            this.alignmentWeight = value;
        } finally {
            alignmentLock.unlock();
        }
    }

    public void setCohesionWeight(double value) {
        cohesionLock.lock();
        try {
            this.cohesionWeight = value;
        } finally {
            cohesionLock.unlock();
        }
    }

    public double getSeparationWeight() {
        separationLock.lock();
        try {
            return separationWeight;
        } finally {
            separationLock.unlock();
        }
    }

    public double getCohesionWeight() {
        cohesionLock.lock();
        try {
            return cohesionWeight;
        } finally {
            cohesionLock.unlock();
        }
    }

    public double getAlignmentWeight() {
        alignmentLock.lock();
        try {
            return alignmentWeight;
        } finally {
            alignmentLock.unlock();
        }
    }
    
    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAvoidRadius() {
        return avoidRadius;
    }

    public double getPerceptionRadius() {
        return perceptionRadius;
    }

    public void createSimulation(int numBoids) {
        this.setAlignmentWeight(initialAlignmentWeight);
        this.setCohesionWeight(initialCohesionWeight);
        this.setSeparationWeight(initialSeparationWeight);

        for (int i = 0; i < numBoids; i++) {
        	P2d pos = new P2d(-width/2 + Math.random() * width, -height/2 + Math.random() * height);
        	V2d vel = new V2d(Math.random() * maxSpeed/2 - maxSpeed/4, Math.random() * maxSpeed/2 - maxSpeed/4);
        	boids.add(new Boid(pos, vel));
        }
    }

    public void stopSimulation() {
        boids.clear();
    }
}


// package pcd.ass01;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// public class BoidsModel {
    
//     private final List<Boid> boids;
//     private volatile double separationWeight; 
//     private volatile double alignmentWeight; 
//     private volatile double cohesionWeight; 
//     private final double width;
//     private final double height;
//     private final double maxSpeed;
//     private final double perceptionRadius;
//     private final double avoidRadius;

//     public BoidsModel(int nboids,  
//     						double initialSeparationWeight, 
//     						double initialAlignmentWeight, 
//     						double initialCohesionWeight,
//     						double width, 
//     						double height,
//     						double maxSpeed,
//     						double perceptionRadius,
//     						double avoidRadius){
//         separationWeight = initialSeparationWeight;
//         alignmentWeight = initialAlignmentWeight;
//         cohesionWeight = initialCohesionWeight;
//         this.width = width;
//         this.height = height;
//         this.maxSpeed = maxSpeed;
//         this.perceptionRadius = perceptionRadius;
//         this.avoidRadius = avoidRadius;
        
//     	boids = new ArrayList<>();
//         for (int i = 0; i < nboids; i++) {
//         	P2d pos = new P2d(-width/2 + Math.random() * width, -height/2 + Math.random() * height);
//         	V2d vel = new V2d(Math.random() * maxSpeed/2 - maxSpeed/4, Math.random() * maxSpeed/2 - maxSpeed/4);
//         	boids.add(new Boid(pos, vel));
//         }

//     }
    
//     public synchronized List<Boid> getBoids(){
//     	return boids;
//     }
    
//     public synchronized double getMinX() {
//     	return -width/2;
//     }

//     public synchronized double getMaxX() {
//     	return width/2;
//     }

//     public synchronized double getMinY() {
//     	return -height/2;
//     }

//     public synchronized double getMaxY() {
//     	return height/2;
//     }
    
//     public synchronized double getWidth() {
//     	return width;
//     }
 
//     public synchronized double getHeight() {
//     	return height;
//     }

//     public synchronized void setSeparationWeight(double value) {
//     	this.separationWeight = value;
//     }

//     public synchronized void setAlignmentWeight(double value) {
//     	this.alignmentWeight = value;
//     }

//     public synchronized void setCohesionWeight(double value) {
//     	this.cohesionWeight = value;
//     }

//     public synchronized double getSeparationWeight() {
//     	return separationWeight;
//     }

//     public synchronized double getCohesionWeight() {
//     	return cohesionWeight;
//     }

//     public synchronized double getAlignmentWeight() {
//     	return alignmentWeight;
//     }
    
//     public synchronized double getMaxSpeed() {
//     	return maxSpeed;
//     }

//     public synchronized double getAvoidRadius() {
//     	return avoidRadius;
//     }

//     public synchronized double getPerceptionRadius() {
//     	return perceptionRadius;
//     }
// }

