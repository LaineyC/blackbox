package pers.laineyc.blackbox.util;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ShellUtil {

    private static String CHARSET;
    static{
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            CHARSET = "GBK";
        }
        else if (osName.contains("linux")) {
            CHARSET = "UTF-8";
        }
        else if (osName.contains("mac")) {
            CHARSET = "UTF-8";
        }
    }

    public static CommandResult exec(Command command) {
        String cmd = command.cmd();
        InputStreamReader stdISR = null;
        InputStreamReader errISR = null;
        Process process = null;
        Integer code = null;
        StringBuilder message = null;
        StringBuilder stdout = null;
        StringBuilder stderr = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            code = process.waitFor();

            String line;
            stdISR = new InputStreamReader(process.getInputStream(), CHARSET);
            BufferedReader stdBR = new BufferedReader(stdISR);
            while ((line = stdBR.readLine()) != null) {
                if(stdout == null) {
                    stdout = new StringBuilder();
                }
                stdout.append(line).append(System.getProperty("line.separator"));
            }

            errISR = new InputStreamReader(process.getErrorStream(), CHARSET);
            BufferedReader errBR = new BufferedReader(errISR);
            while ((line = errBR.readLine()) != null) {
                if(stderr == null) {
                    stderr = new StringBuilder();
                }
                stderr.append(line).append(System.getProperty("line.separator"));
            }
        }
        catch (IOException | InterruptedException e) {
            message = new StringBuilder();
            message.append(e.getMessage());
        }
        finally {
            try {
                if (stdISR != null) {
                    stdISR.close();
                }
                if (errISR != null) {
                    errISR.close();
                }
                if (process != null) {
                    process.destroy();
                }
            }
            catch (IOException e) {
                if(message == null) {
                    message = new StringBuilder();
                }
                message.append(e.getMessage());
            }
        }

        return new CommandResult(code, stdout != null ? stdout.toString() : null, stderr != null ? stderr.toString() : null, message != null ? message.toString() : null);
    }

    public record Command(String cmd){

    }

    public record CommandResult(Integer code, String stdout, String stderr, String message) {

    }

}
