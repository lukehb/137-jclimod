package onethreeseven.jclimod.command;

import com.beust.jcommander.Parameter;
import onethreeseven.jclimod.CLICommand;

/**
 * A command to exit the program and return control the the running shell/terminal/command-line etc.
 * @author Luke Bermingham
 */
public class Exit extends CLICommand {

    @Parameter(names = {"-t", "--delayTime"}, description = "How many seconds to wait before exiting.")
    private int delayTimeSeconds = 0;

    @Override
    protected String getUsage() {
        return "exit";
    }

    @Override
    protected boolean parametersValid() {
        return true;
    }

    @Override
    protected boolean runImpl() {
        if(delayTimeSeconds > 0){
            startDelayedExit();
        }
        else{
            exit();
        }
        return true;
    }

    private void startDelayedExit(){
        Thread shutdownThread = new Thread(new Runnable() {
            final long startTime = System.currentTimeMillis();
            final long delayTimeSecs = delayTimeSeconds;
            @Override
            public void run() {
                System.out.println("Program will exit in " + delayTimeSecs + " seconds...");
                long elapsedMillis = System.currentTimeMillis() - startTime;
                long elapsedSeconds = elapsedMillis / 1000L;
                while(elapsedSeconds < delayTimeSecs){
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    elapsedMillis = System.currentTimeMillis() - startTime;
                    elapsedSeconds = elapsedMillis / 1000L;
                }
                exit();
            }
        });
        shutdownThread.setName("CLI exit countdown thread");
        shutdownThread.start();
    }

    protected void exit(){
        System.exit(0);
    }

    @Override
    protected void resetParametersAfterRun(Class clazz) {
        super.resetParametersAfterRun(clazz);
        delayTimeSeconds = 0;
    }

    @Override
    public boolean shouldStoreRerunAlias() {
        return delayTimeSeconds > 0;
    }

    @Override
    public String generateRerunAliasBasedOnParams() {
        return delayTimeSeconds + "s";
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getCommandName() {
        return "exit";
    }

    @Override
    public String[] getOtherCommandsNames() {
        return new String[]{"ex"};
    }

    @Override
    public String getDescription() {
        return "Exits the program.";
    }
}
