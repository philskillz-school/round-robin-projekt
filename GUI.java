import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GUI extends JFrame {

    public enum HorizontalAlign {
        LEFT,
        CENTER,
        RIGHT
    }
    public enum VerticalAlign {
        TOP,
        CENTER,
        BOTTOM
    }

    /**
     * Panel to display the process info
     */
    public static class ProcessGridPane extends JPanel {
        private final RoundRobin robin;

        private final int cols = 6;
        final int timeQuantum = 3; // am besten nur ungerade zahlen


        final int barStartX = 10;
        final int barStartY = 100;
        final int barEndY = 210;
        final int barEndX = 775;

        final int gridStartY = 280;
        final int gridStartX = 10;
        final int gridEndX = 775;
        final int gridEndY = 550;


        public ProcessGridPane() {
            List<Process> processes = new ArrayList<>();
            processes.add(new Process("P1", 7));
            processes.add(new Process("P2", 4));
            processes.add(new Process("P3", 4, timeQuantum*2));
            processes.add(new Process("P4", 6, (int)(timeQuantum*1.5)));
            processes.add(new Process("P5", 7, 4));
            processes.add(new Process("P6", 4));
            this.robin = new RoundRobin(processes, timeQuantum);


            robin.executeAll();
        }

        /**
         * Rounds a double to a specified precision
         * @param value Value to round
         * @param precision Precision
         * @return Rounded value
         */
        public static double round(double value, int precision) {
            int scale = (int) Math.pow(10, precision);
            return (double) Math.round(value * scale) / scale;
        }

        /**
         * Draws a string with advanced alignment options
         * @param g Graphics instance
         * @param text Text to draw
         * @param bounds Bounds of the text
         * @param hAlign Horizontal alignment
         * @param vAlign Vertical alignment
         * @param font Font to use
         */
        public static void drawStringAdvanced(Graphics g, String text, Rectangle bounds, HorizontalAlign hAlign, VerticalAlign vAlign, Font font) {
            // Get the FontMetrics
            FontMetrics metrics = g.getFontMetrics(font);


            int sWidth = metrics.stringWidth(text);
            int x;
            int y;

            if (hAlign == HorizontalAlign.LEFT) {
                x = bounds.x;
            } else if (hAlign == HorizontalAlign.RIGHT) {
                x = bounds.x + bounds.width - sWidth;
            } else { // default to center
                x = bounds.x + (bounds.width - sWidth) / 2;
            }

            if (vAlign == VerticalAlign.TOP) {
                y = bounds.y + metrics.getAscent();
            } else if (vAlign == VerticalAlign.BOTTOM) {
                y = bounds.y + bounds.height ; //- metrics.getHeight();
            } else { // default to center
                y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
            }

            Font oldFont = g.getFont();
            g.setFont(font);
            // Draw the String
            g.drawString(text, x, y);
            g.setFont(oldFont);
        }

        /**
         * Draws a bar chart of the processes
         * @param g2d Graphics2D instance
         * @param startX Start X coordinate
         * @param startY Start Y coordinate
         * @param endX End X coordinate
         * @param endY End Y coordinate
         */
        @SuppressWarnings("SameParameterValue")
        void drawProcessBar(Graphics2D g2d, int startX, int startY, int endX, int endY) {
            int totalBurstTime = robin.processes.stream().mapToInt(Process::getBurstTimeOriginal).sum();
            int totalWidth = endX - startX;
            int height = endY - startY;

            g2d.setColor(Color.BLACK);
            g2d.drawRect(startX, startY, totalWidth, height);

            g2d.setColor(Color.CYAN);
            g2d.fillRect(startX+1, startY+1, totalWidth-1, 30);
            g2d.setColor(Color.BLACK);
            drawStringAdvanced(g2d,  "<<<<    " + totalBurstTime + " units    >>>>", new Rectangle(startX, startY, totalWidth, 30), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 16f));
            startY += 30;
            height -= 30;

            totalWidth -= 1; // subtract border width
            height -= 1; // subtract border width
            startY += 1;



            int x = startX+1; // width for border
            for (Process p : robin.processes) {
                float ratio = p.getBurstTimeOriginal() / ((float) totalBurstTime);
                int width = (int) (ratio * totalWidth);
                g2d.setColor(p.getColor());
                g2d.fillRect(x, startY, width, height);

                g2d.setColor(Color.BLACK);

                Rectangle bounds = new Rectangle(x, startY+height/4, width, height/2);
                drawStringAdvanced(g2d, p.getName(), bounds, HorizontalAlign.CENTER, VerticalAlign.TOP, g2d.getFont());
                drawStringAdvanced(g2d, p.getBurstTimeOriginal() + "u (" + round(ratio*100, 1) + "%)", bounds, HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());
                drawStringAdvanced(g2d, p.getStartTime() + "u delay", bounds, HorizontalAlign.CENTER, VerticalAlign.BOTTOM, g2d.getFont());
                x += width;
            }

            if (x < endX) {
                g2d.setColor(robin.processes.get(robin.processes.size()-1).getColor());
                g2d.fillRect(x, startY, endX-x, height);
            }
        }

        /**
         * Formats the process quantums into a grid
         * @return 2D List of ProcessQuantum instances
         */
        private List<List<ProcessQuantum>> formatQuantums() {
            final int maxQuantumsPerRow = cols*robin.quantum;
            int quantumsElapsedInRow = 0;
            List<List<ProcessQuantum>> formatted = new ArrayList<>();

            List<ProcessQuantum> currentRow = new ArrayList<>();
            for (ProcessQuantum pq : robin.executeAll()) {
                if (quantumsElapsedInRow+pq.quantum <= maxQuantumsPerRow) {
                    currentRow.add(pq);
                    quantumsElapsedInRow += pq.quantum;
                } else {
                    int remaining = maxQuantumsPerRow - quantumsElapsedInRow;
                    if (remaining > 0) {
                        currentRow.add(new ProcessQuantum(pq.process, remaining)); // add the remaining quantum
                    }
                    quantumsElapsedInRow = 0;
                    formatted.add(currentRow);
                    currentRow = new ArrayList<>();
                    currentRow.add(new ProcessQuantum(pq.process, pq.quantum - remaining)); // add the remaining quantum
                    quantumsElapsedInRow += pq.quantum - remaining;
                }
            }

            if (!currentRow.isEmpty()) {
                formatted.add(currentRow);
            }

            return formatted;
        }

        /**
         * Draws a row of processes in the grid
         * @param g2d Graphics2D instance
         * @param pqs List of ProcessQuantum instances
         * @param row Row index
         * @param startX Start X coordinate
         * @param startY Start Y coordinate
         * @param quantumWidth Width of a quantum
         * @param cellHeight Height of a cell
         */
        void drawRow(Graphics2D g2d, List<ProcessQuantum> pqs, int row, int startX, int startY, int quantumWidth, int cellHeight) {
            int quantumsElapsed = row*cols*robin.quantum;

            g2d.setColor(Color.BLACK);

            int rowQuantumsElapsed = 0;
            //noinspection ForLoopReplaceableByForEach
            for (int i=0; i<pqs.size(); i++) {
                ProcessQuantum pq = pqs.get(i);

                int cellPosX = startX + rowQuantumsElapsed * quantumWidth;
                int cellWidth = pq.quantum * quantumWidth;

                g2d.setColor(Color.BLACK);
                g2d.drawRect(cellPosX, startY, cellWidth, cellHeight);

                g2d.setColor(pq.process.getColor());
                g2d.fillRect(cellPosX+1, startY+1, cellWidth-1, cellHeight-1);

                g2d.setColor(Color.BLACK);
                drawStringAdvanced(g2d, pq.process.getName(), new Rectangle(cellPosX, startY, cellWidth, cellHeight-10), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());
                drawStringAdvanced(g2d, pq.quantum + "u", new Rectangle(cellPosX, startY+10, cellWidth, cellHeight), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont());

                quantumsElapsed += pq.quantum;
                rowQuantumsElapsed += pq.quantum;

                drawStringAdvanced(g2d, String.valueOf(quantumsElapsed), new Rectangle(cellPosX, startY, cellWidth-5, cellHeight-5), HorizontalAlign.RIGHT, VerticalAlign.BOTTOM, g2d.getFont());
            }

//            drawStringAdvanced(g2d, String.valueOf(quantumsElapsed-rowQuantumsElapsed), new Rectangle(startX+5+10, startY, quantumWidth-5-10, cellHeight-5), HorizontalAlign.LEFT, VerticalAlign.BOTTOM, g2d.getFont());
        }


        /**
         * Draws the process start bars on the left side on each quantum where a process starts
         * If multiple processes start at the same time, the available space is split and the vertical bars are stacked
         * @param g2d Graphics2D instance
         * @param startX Start X coordinate
         * @param startY Start Y coordinate
         * @param quantumWidth Width of a quantum
         * @param cellHeight Height of a cell
         */
        void drawProcessStartBars(Graphics2D g2d, int startX, int startY, int quantumWidth, int cellHeight) {
            Map<Integer, List<Process>> groupedByStartTime = robin.processes.stream()
                    .collect(Collectors.groupingBy(Process::getStartTime));

            groupedByStartTime.forEach((startTime, processes) -> {
                if (processes == null) {
                    return;
                }

                final int xStart = startX + (startTime % (cols*robin.quantum)) * quantumWidth;
                final int barHeight = cellHeight/processes.size();
                final int barWidth = 10;

                int y = startY + 1;
                //noinspection ForLoopReplaceableByForEach
                for (int i=0; i<processes.size(); i++) {
                    Process p = processes.get(i);

                    g2d.setColor(p.getColor());
                    g2d.fillRect(xStart+1, y, barWidth, Math.min(barHeight, startY + cellHeight - y));
                    y += barHeight;
                }

                // draw line to separate the bars from the grid
                g2d.setColor(Color.BLACK);
                g2d.drawLine(xStart, startY, xStart, startY+cellHeight);
                g2d.drawLine(xStart+barWidth, startY, xStart+barWidth, startY+cellHeight);
            });
        }

        /**
         * Draws a grid of the processes
         * @param g2d Graphics2D instance
         * @param startX Start X coordinate
         * @param startY Start Y coordinate
         * @param endX End X coordinate
         * @param endY End Y coordinate
         */
        @SuppressWarnings("SameParameterValue")
        void drawGrid(Graphics2D g2d, int startX, int startY, int endX, int endY) {
            g2d.setColor(Color.BLACK);
            drawStringAdvanced(g2d, "Prozessabfolge", new Rectangle(startX, startY-30, endX, 20), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 20f));
            List<List<ProcessQuantum>> pqs = formatQuantums();

            int panelHeight = endY - startY; // Höhe des Panels
            int cellHeight = panelHeight / pqs.size(); // Höhe einer Zelle
            int quantumWidth = (endX - startX) / (cols*robin.quantum);

            for (int i=0; i<pqs.size(); i++) {
                drawRow(g2d, pqs.get(i), i, startX, startY + i*cellHeight, quantumWidth, cellHeight);
            }

            drawProcessStartBars(g2d, startX, startY, quantumWidth, cellHeight);
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            drawStringAdvanced(g2d, "Round Robin", new Rectangle(0, 0, 800, 40), HorizontalAlign.CENTER, VerticalAlign.CENTER, g2d.getFont().deriveFont(Font.BOLD, 24f));

            g2d.drawString("Quantum: " + robin.quantum + " units", 10, 40);
            g2d.drawString("Prozesse (" + robin.processes.size() +"): ", 10, 60);

            drawProcessBar(g2d, barStartX, barStartY, barEndX, barEndY);
            drawGrid(g2d, gridStartX, gridStartY, gridEndX, gridEndY);

            g2d.dispose();
        }

    }

    public GUI() {
        super();

        initialize();

    }


    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        this.setSize(800, 600);

        this.setTitle("Round Robin");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("skz logo.png")));

        this.add(new ProcessGridPane());
        this.setVisible(true);
    }
}