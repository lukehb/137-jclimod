package onethreeseven.jclimod.command;

import onethreeseven.jclimod.CLICommand;

/**
 * A command to exit the program and return control the the running shell/terminal/command-line etc.
 * @author Luke Bermingham
 */
public class Exit extends CLICommand {

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
        System.exit(0);
        return true;
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
    public String[] getCommandNameAliases() {
        return new String[]{"ex"};
    }

    @Override
    public String getDescription() {
        return "Exits the program.";
    }
}
