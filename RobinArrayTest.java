public class RobinArrayTest {
    final int processCount = 5;
    final int quantum = 2;

    final int[][] processes = new int[processCount][1]; // [burst time]

    public RobinArrayTest() {
        for (int i = 0; i < processCount; i++) { // fill processes with random burst times
            processes[i][0] = (int) (Math.random() * 10) + 1;
        }

        final int[][] pr = processes.clone();

        while (true) {
            boolean allExecuted = false;

            for (int i=0; i<pr.length; i++) {
                int[] process = pr[i];

                if (process[0] > 0) {
                    int toExecute = Math.min(quantum, process[0]); // 3, 9 -> 3
                    // 3, 8 -> 3
                    // 3, 5 -> 3
                    // 3, 2 -> 2

                    process[0] -= toExecute;
                    System.out.println("Process " + i + " executed for " + toExecute + " units");

                    if (process[0] <= 0) {
                        System.out.println("Process " + i + " is done");
                        allExecuted = true;
                    } else {
                        allExecuted = false;
                    }

                } else {
                    allExecuted = true;
                }
                i++;
            }

            if (allExecuted) {
                break;
            }
        }

        System.out.println("All processes are done");
    }

    public static void main(String[] args) {
        new RobinArrayTest();
    }
}
