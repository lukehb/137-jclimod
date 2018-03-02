package onethreeseven.jclimod;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;

import java.io.*;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A command-line interface (CLI) program.
 * @author Luke Bermingham
 */
public class CLIProgram {

    protected final JCommander jc = new JCommander();
    private final ThreadFactory threadFactory = r -> new Thread(r, "Poll for CLI input thread.");
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);
    private final AtomicBoolean keepReadingInput = new AtomicBoolean(true);
    private Future readingInputTask;

    /**
     * Calling this version of the constructor does not go looking for any commands to register.
     * I.e the CLI has no commands until the user adds some.
     */
    public CLIProgram(){
        jc.setAllowParameterOverwriting(true);
    }

    public CLIProgram(Object[] modelDataForCommands){
        this();
        findAndRegisterCommands(modelDataForCommands);
    }

    public boolean doCommand(String[] args){

        boolean success = false;

        try{
            jc.parse(args);
            String commandName = jc.getParsedCommand();

            if(jc.getCommands().containsKey(commandName)){
                //run the command
                List<Object> commandObjs = jc.getCommands().get(commandName).getObjects();
                for (Object commandObj : commandObjs) {
                    if(commandObj instanceof CLICommand){
                        CLICommand cmd = (CLICommand) commandObj;
                        //check if we should run the help command
                        if(cmd.askedForHelp()){
                            StringBuilder sb = new StringBuilder();
                            sb.append(cmd.getDescription());
                            sb.append("\n");
                            jc.usage(commandName, sb);
                            System.out.println(sb.toString());
                        }
                        //run the command
                        success = cmd.run(args);
                    }
                }
            }
            else{
                System.err.println("No command called: " + commandName);
            }
        }catch (ParameterException e){
            System.err.println("Invalid parameters: " + e.getMessage());
            success = false;
        }catch (Exception e){
            System.err.println("Command failed: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    public CLIProgram addCommand(CLICommand command){
        String[] secondaryCommandNames = command.getOtherCommandNames();
        String primaryCommandName = command.getCommandName();
        if(secondaryCommandNames == null || secondaryCommandNames.length <= 0){
            jc.addCommand(primaryCommandName, command);
            jc.getCommands().get(primaryCommandName).setAllowParameterOverwriting(true);
        }else {
            jc.addCommand(primaryCommandName, command, secondaryCommandNames);
            jc.getCommands().get(primaryCommandName).setAllowParameterOverwriting(true);
        }
        return this;
    }

    /**
     * Searches the modules of the application for commands and registers them.
     */
    private void findAndRegisterCommands(Object... modelDataForCommands){

        //use the service loader
        ServiceLoader<AbstractCommandsListing> serviceLoader = ServiceLoader.load(AbstractCommandsListing.class);
        for (AbstractCommandsListing commandListing : serviceLoader) {
            for (CLICommand command : commandListing.createCommands(jc, modelDataForCommands)) {
                addCommand(command);
            }
        }
    }

    public void startListeningForInput(){



        readingInputTask = executorService.submit(()->{

            final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            try{

                while(keepReadingInput.get()){
                    //ready() is very important here, because buffer is only ready if it has input in it
                    //otherwise if ready() were removed we go to readline() which just blocks for input
                    //which will cause this thread to block while holding System.in (a main thread resource)
                    if(br.ready()){

                        String inputStr = br.readLine();

                        String[] args = inputStr.split(" ");
                        try {
                            boolean success = this.doCommand(args);
                            if (!success) {
                                System.err.println("Command failed.");
                            }
                        }
                        catch (MissingCommandException e){
                            System.err.println(inputStr + " is not a valid command. Try typing lc to list all valid commands.");
                        }
                        catch (ParameterException e){
                            System.err.println("The following parameters are invalid for that command: " + e.getMessage());
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }

                br.close();

            }catch (IOException ex){
                System.out.println("Stopped reading console input.");
            }

        });
    }

    public void shutdown(){
        keepReadingInput.set(false);

        if(readingInputTask != null){
            readingInputTask.cancel(true);
        }

        executorService.shutdownNow();
    }

}
