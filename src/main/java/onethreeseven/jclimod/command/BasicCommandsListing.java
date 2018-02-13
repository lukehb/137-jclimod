package onethreeseven.jclimod.command;

import com.beust.jcommander.JCommander;
import onethreeseven.jclimod.AbstractCommandsListing;
import onethreeseven.jclimod.CLICommand;

/**
 * Some basic commands that all CLI programs can use.
 * Note: This class, and by extension these commands, are added to the program using "provides" keyword in the module-info.java file.
 * @author Luke Bermingham
 */
public class BasicCommandsListing extends AbstractCommandsListing {
    @Override
    protected CLICommand[] createCommands(JCommander jc, Object... args) {
        return new CLICommand[]{
                new ListCommands(jc),
                new Exit()
        };
    }
}
