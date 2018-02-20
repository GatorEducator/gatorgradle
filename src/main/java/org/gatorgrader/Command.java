import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements Runnable {
    private List<String> command;
    private boolean outputToSysOut = false;

    public Command(String... command) {
        this.command = new ArrayList<>(Arrays.asList(command));
    }

    public Command(List<String> args) {
        this.command = args;
    }

    public Command() {
        this.command = new ArrayList<>();
    }

    public Command with(String... command) {
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    public void setOutputToSysOut(boolean val) {
        outputToSysOut = val;
    }

    /**
     * Execute the Command.
     *
     * @param block should this method block until the command finishes?
     */
    public void execute(boolean block) {
        if (block) {
            run();
        } else {
            new Thread(this).start();
        }
    }

    /**
     * Run the Command (execute provides better control, and should be called instead of run).
     *
     */
    public void run() {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process proc = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            proc.waitFor();

            int exitVal = proc.exitValue();

        } catch (InterruptedException | IOException ex) {
            System.err.println("Error while grading: " + ex);
            ex.printStackTrace();
        }
    }
}
