package onethreeseven.jclimod;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import java.util.List;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A command-line interface (CLI) program.
 * @author Luke Bermingham
 */
public class CLIProgram {

    protected final JCommander jc = new JCommander();

    public CLIProgram(Object... modelDataForCommands){
        jc.setAllowParameterOverwriting(true);
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
                        success = cmd.run();
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

    public void addCommand(CLICommand command){
        String[] secondaryCommandNames = command.getCommandNameAliases();
        String primaryCommandName = command.getCommandName();
        if(secondaryCommandNames == null || secondaryCommandNames.length <= 0){
            jc.addCommand(primaryCommandName, command);
            jc.getCommands().get(primaryCommandName).setAllowParameterOverwriting(true);
        }else {
            jc.addCommand(primaryCommandName, command, secondaryCommandNames);
            jc.getCommands().get(primaryCommandName).setAllowParameterOverwriting(true);
        }
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
        ExecutorService exec = Executors.newSingleThreadExecutor();
        final AtomicBoolean keepReadingInput = new AtomicBoolean(true);
        final Scanner scanner = new Scanner(System.in);

        exec.execute(()->{
            while(keepReadingInput.get()){
                while(scanner.hasNextLine()){
                    String inputStr = scanner.nextLine();
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
            scanner.close();
        });
    }

}
