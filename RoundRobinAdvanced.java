import java.util.*;

public class RoundRobinAdvanced {
    public final int timeQuantum;
    public final List<Process> processList;
    public final Queue<Process> processQueue;
    public final List<Process> delayedProcessQueue;

    public final List<ProcessQuantum> executed;
    private int currentTime = 0;

    /**
     * @return The current time of the scheduler
     */
    public int getCurrentTime() {
        return currentTime;
    }

    /**
     * Creates a new Round Robin scheduler with the given time quantum and process list
     * @param timeQuantum Time quantum for each process
     * @param processList List of processes
     */
    public RoundRobinAdvanced(int timeQuantum, List<Process> processList) {
        this.timeQuantum = timeQuantum;
        this.processList = processList;
        this.processList.sort(Comparator.comparingInt(Process::getStartTime));

        this.processQueue = new LinkedList<>();
        this.delayedProcessQueue = new ArrayList<>();
        for (Process process : processList) {
            if (process.getStartTime() == 0) {
                processQueue.offer(process);
            } else {
                delayedProcessQueue.add(process);
            }
        }
//        delayedProcessQueue.sort(Comparator.comparingInt(Process::getStartTime));

        // processQueue contains all processes that have started at time 0
        // delayedProcesses contains all processes that have not started at time 0

        this.executed = new ArrayList<>();
    }

    public void addProcess(Process process) {
        processList.add(process);
        processList.sort(Comparator.comparingInt(Process::getStartTime));
        if (process.getStartTime() == 0) {
            processQueue.offer(process);
        } else {
            delayedProcessQueue.add(process);
        }
    }

    /**
     * Randomizes the colors of all processes
     */
    public void randomizeColors() {
        processList.forEach(Process::randomizeColor);
    }

    /**
     * Resets the progress of the scheduler
     */
    public void resetProgress() {
        this.processQueue.clear();
        this.delayedProcessQueue.clear();
        this.currentTime = 0;

        processList.forEach(Process::reset); // set all processes to initial state

        processList.forEach(p -> {
            if (p.getStartTime() == this.currentTime) {
                processQueue.offer(p);
            } else {
                delayedProcessQueue.add(p);
            }
        });

        this.executed.clear();
    }

    /**
     * Executes one time quantum of the current process
     */
    public void executeOne() {
        if (processQueue.isEmpty()) {
            return;
        }

        Process currentProcess = processQueue.poll();
        ProcessQuantum pq = currentProcess.execute(timeQuantum);
        executed.add(pq);
        currentTime += pq.quantum; // increment time by time quantum

        // add processes that can now start
        Iterator<Process> iterator = delayedProcessQueue.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            if (p.getStartTime() <= currentTime) {
                processQueue.offer(p);
                iterator.remove();
            } else { // since the list is sorted, we can break here
                break;
            }
        }

        if (!currentProcess.isDone()) { // push back to the end of the queue if not done
            processQueue.offer(currentProcess);
        }
    }

    /**
     * Executes all processes in the queue
     */
    public void executeAll() {
        while (!processQueue.isEmpty()) {
            executeOne();
        }
    }
}
