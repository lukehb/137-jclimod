package onethreeseven.jclimod.command;

import com.beust.jcommander.JCommander;
import onethreeseven.jclimod.CLICommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Command to list all registered commands in the application.
 * @author Luke Bermingham
 */

public class ListCommands extends CLICommand {

    private static final String commandHeader = "[%s Commands]";

    private final JCommander jc;

    public ListCommands(JCommander jc){
        this.jc = jc;
    }

    @Override
    protected String getUsage() {
        return "lc";
    }

    @Override
    protected boolean parametersValid() {
        return true;
    }

    /**
     * @return A map containing a list of commands under each category.
     */
    public HashMap<String, ArrayList<CLICommand>> getMapOfCommands(){
        HashMap<String, ArrayList<CLICommand>> commands = new HashMap<>();

        for (Map.Entry<String, JCommander> entry : jc.getCommands().entrySet()) {

            for (Object commandObj : entry.getValue().getObjects()) {
                if(commandObj instanceof CLICommand){
                    CLICommand command = (CLICommand) commandObj;
                    ArrayList<CLICommand> commandList = commands.computeIfAbsent(command.getCategory(), k -> new ArrayList<>());
                    commandList.add(command);
                }
            }
        }
        return commands;
    }

    @Override
    protected boolean runImpl() {
        //output the commands list
        for (Map.Entry<String, ArrayList<CLICommand>> entry : getMapOfCommands().entrySet()) {
            System.out.println(String.format(commandHeader, entry.getKey()));
            for (CLICommand tsCommand : entry.getValue()) {
                System.out.println("    " + tsCommand.toString());
            }
        }
        return true;
    }

    @Override
    public boolean shouldStoreRerunAlias() {
        return false;
    }

    @Override
    public String generateRerunAliasBasedOnParams() {
        return null;
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getCommandName() {
        return "listCommands";
    }

    @Override
    public String[] getOtherCommandsNames() {
        return new String[]{"lc"};
    }

    @Override
    public String getDescription() {
        return "List all currently registered commands in the application.";
    }
}
