package onethreeseven.jclimod;


import com.beust.jcommander.JCommander;

/**
 * The base class for exposing a collection of commands to the {@link CLIProgram}.
 * This class "used" in this module so other modules may provide
 * back concrete versions that can be loaded in through the jdk service loader.
 * @author Luke Bermingham
 */
public abstract class AbstractCommandsListing {
    protected abstract CLICommand[] createCommands(JCommander jc, Object... args);
}
