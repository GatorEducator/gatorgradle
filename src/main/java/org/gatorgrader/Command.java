import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Command implements Runnable {
    private List<String> command;

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

    public void execute(boolean block) {
        if (block) {
            run();
        } else {
            new Thread(this).start();
        }
    }

    public void run() {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process proc = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            proc.waitFor();

            int exitVal = proc.exitValue();

        } catch (InterruptedException | IOException ex) {
        }
    }
}
